<?php
require_once 'GeminiService.php';

class RegistroController {
    private $conn;

    public function __construct($db) {
        $this->conn = $db;
    }
    // 1. OBTENER CATÁLOGOS (GET)
    public function obtenerAlimentos() {
        $stmt = $this->conn->prepare("SELECT * FROM foods");
        $stmt->execute();
        http_response_code(200);
        echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));
    }

    public function obtenerActividades() {
        $stmt = $this->conn->prepare("SELECT * FROM activities");
        $stmt->execute();
        http_response_code(200);
        echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));
    }

    // 2. GUARDAR BALANCE MANUAL (POST)
    public function guardarBalanceDiario($data) {
        $net_balance = $data->calories_in - $data->calories_out;
        
        $status = 'BALANCEADO';
        if($net_balance > 300) $status = 'EXCESIVO';
        if($net_balance < -300) $status = 'DEFICIENTE';

        $query = "INSERT INTO energy_balance (user_id, record_date, bmi, calories_in, calories_out, net_balance, energy_status) 
                  VALUES (:user_id, :record_date, :bmi, :cal_in, :cal_out, :net, :status)";
        
        $stmt = $this->conn->prepare($query);
        $stmt->execute([
            ':user_id' => $data->user_id,
            ':record_date' => $data->record_date,
            ':bmi' => $data->bmi,
            ':cal_in' => $data->calories_in,
            ':cal_out' => $data->calories_out,
            ':net' => $net_balance,
            ':status' => $status
        ]);

        http_response_code(201);
        echo json_encode([
            "mensaje" => "Balance diario guardado",
            "net_balance" => $net_balance,
            "status" => $status
        ]);
    }

    // 3. GUARDAR DÍA COMPLETO AUTOMATIZADO Y LLAMAR A GEMINI (POST)
    public function guardarRegistroCompleto($data) {
        try {
            $this->conn->beginTransaction();

            // EL ESCUDO PROTECTOR APLICADO
            $user_id = $data->user_id;
            $record_date = $data->record_date ?? date('Y-m-d'); 

            // 1. Obtener datos del usuario
            $stmtUser = $this->conn->prepare("SELECT height_cm, weight_kg FROM users WHERE id = :user_id");
            $stmtUser->execute([':user_id' => $user_id]);
            $user = $stmtUser->fetch(PDO::FETCH_ASSOC);

            if (!$user) {
                throw new Exception("Usuario no encontrado");
            }

            $altura_m = $user['height_cm'] / 100;
            $bmi = round($user['weight_kg'] / ($altura_m * $altura_m), 2);

            $calories_in = 0;
            $calories_out = 0;
            
            // Variables para guardar el texto que le enviaremos a Gemini
            $resumenAlimentosText = "";
            $resumenActividadesText = "";

            // 2. Procesar Alimentos
            if (isset($data->alimentos_consumidos) && is_array($data->alimentos_consumidos)) {
                $queryFoodLog = "INSERT INTO food_logs (user_id, record_date, food_id, quantity) VALUES (:user_id, :record_date, :food_id, :quantity)";
                $stmtFoodLog = $this->conn->prepare($queryFoodLog);
                
                // Modificamos aquí para traer también el 'name'
                $stmtGetFood = $this->conn->prepare("SELECT name, calories_per_unit FROM foods WHERE id = :id");

                foreach ($data->alimentos_consumidos as $alimento) {
                    $stmtFoodLog->execute([
                        ':user_id' => $user_id,
                        ':record_date' => $record_date,
                        ':food_id' => $alimento->food_id,
                        ':quantity' => $alimento->quantity
                    ]);

                    $stmtGetFood->execute([':id' => $alimento->food_id]);
                    $foodData = $stmtGetFood->fetch(PDO::FETCH_ASSOC);
                    if ($foodData) {
                        $calories_in += ($foodData['calories_per_unit'] * $alimento->quantity);
                        // Agregamos el alimento al resumen de Gemini
                        $resumenAlimentosText .= "- " . $foodData['name'] . " (Cant: " . $alimento->quantity . ")\n";
                    }
                }
            }

            // 3. Procesar Actividades
            if (isset($data->actividades_realizadas) && is_array($data->actividades_realizadas)) {
                $queryActLog = "INSERT INTO activity_logs (user_id, record_date, activity_id, minutes) VALUES (:user_id, :record_date, :activity_id, :minutes)";
                $stmtActLog = $this->conn->prepare($queryActLog);
                
                // Modificamos aquí para traer también el 'name'
                $stmtGetAct = $this->conn->prepare("SELECT name, calories_per_minute FROM activities WHERE id = :id");

                foreach ($data->actividades_realizadas as $actividad) {
                    $stmtActLog->execute([
                        ':user_id' => $user_id,
                        ':record_date' => $record_date,
                        ':activity_id' => $actividad->activity_id,
                        ':minutes' => $actividad->minutes
                    ]);

                    $stmtGetAct->execute([':id' => $actividad->activity_id]);
                    $actData = $stmtGetAct->fetch(PDO::FETCH_ASSOC);
                    if ($actData) {
                        $calories_out += ($actData['calories_per_minute'] * $actividad->minutes);
                        // Agregamos la actividad al resumen de Gemini
                        $resumenActividadesText .= "- " . $actData['name'] . " (" . $actividad->minutes . " minutos)\n";
                    }
                }
            }

            // 4. Calcular el balance neto
            $net_balance = $calories_in - $calories_out;
            $status = 'BALANCEADO';
            if ($net_balance > 300) $status = 'EXCESIVO';
            if ($net_balance < -300) $status = 'DEFICIENTE';

            // 5. Guardar en energy_balance
            $queryBalance = "INSERT INTO energy_balance (user_id, record_date, bmi, calories_in, calories_out, net_balance, energy_status) 
                             VALUES (:user_id, :record_date, :bmi, :cal_in, :cal_out, :net, :status)";
            $stmtBalance = $this->conn->prepare($queryBalance);
            $stmtBalance->execute([
                ':user_id' => $user_id,
                ':record_date' => $record_date,
                ':bmi' => $bmi,
                ':cal_in' => $calories_in,
                ':cal_out' => $calories_out,
                ':net' => $net_balance,
                ':status' => $status
            ]);

            // confirmamos inserciones en BD
            $this->conn->commit();

            
            // LLAMADA A LA INTELIGENCIA ARTIFICIAL 
            
            $gemini = new GeminiService();
            
            // Le pasamos TODOS los cálculos para que haga un análisis profundo
            $consejoIA = $gemini->obtenerRecomendacion(
                $resumenAlimentosText, 
                $resumenActividadesText,
                $calories_in,
                $calories_out,
                $net_balance,
                $bmi,
                $status
            );

            // 7. Retornamos la respuesta a Android incluyendo el mensaje de la IA
            http_response_code(201);
            echo json_encode([
                "mensaje" => "Día registrado y balance calculado con éxito",
                "recomendacion_ia" => $consejoIA, // AQUI VIAJA EL CONSEJO PARA ANDROID
                "resumen" => [
                    "bmi" => $bmi,
                    "calories_in" => $calories_in,
                    "calories_out" => $calories_out,
                    "net_balance" => $net_balance,
                    "status" => $status
                ]
            ]);

        } catch (Exception $e) {
            $this->conn->rollBack();
            http_response_code(400);
            echo json_encode(["error" => "Error al procesar el día: " . $e->getMessage()]);
        }
    }
}
?>