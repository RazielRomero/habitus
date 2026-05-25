<?php

class HistorialController {
    private $conn;

    public function __construct($db) {
        $this->conn = $db;
    }

    public function getResumenHoy($user_id) {
        try {
            $fecha_hoy = date('Y-m-d');
            
            // Atrapamos el parámetro 'dias' que manda Android (si no lo manda, usamos 7 por defecto)
            $dias_limite = isset($_GET['dias']) ? (int)$_GET['dias'] : 7;

            // 1. Datos de HOY 
            $qComida = "SELECT COALESCE(SUM(fl.quantity * f.calories_per_unit), 0) as total 
                        FROM food_logs fl 
                        JOIN foods f ON fl.food_id = f.id 
                        WHERE fl.user_id = :u AND DATE(fl.record_date) = :f";
            $stmtC = $this->conn->prepare($qComida);
            $stmtC->execute([':u' => $user_id, ':f' => $fecha_hoy]);
            $consumidas = $stmtC->fetch(PDO::FETCH_ASSOC)['total'];

            $qActividad = "SELECT COALESCE(SUM(al.minutes * a.calories_per_minute), 0) as total 
                           FROM activity_logs al 
                           JOIN activities a ON al.activity_id = a.id 
                           WHERE al.user_id = :u AND DATE(al.record_date) = :f";
            $stmtA = $this->conn->prepare($qActividad);
            $stmtA->execute([':u' => $user_id, ':f' => $fecha_hoy]);
            $quemadas = $stmtA->fetch(PDO::FETCH_ASSOC)['total'];

            // 2. Datos del Historial (Para la gráfica y la tabla)
            $historial_semanal = [];
            
            // Hacemos el ciclo dinámico (ej. 7, 15 o 30 días hacia atrás)
            for ($i = $dias_limite - 1; $i >= 0; $i--) {
                // AQUÍ ESTÁ LA MAGIA: Guardamos la fecha completa en formato YYYY-MM-DD
                $fecha_completa = date('Y-m-d', strtotime("-$i days"));

                // Consultar Comida del día en el bucle
                $stmtC_bucle = $this->conn->prepare($qComida);
                $stmtC_bucle->execute([':u' => $user_id, ':f' => $fecha_completa]);
                $cals_dia = $stmtC_bucle->fetch(PDO::FETCH_ASSOC)['total'];

                // Consultar Ejercicio del día en el bucle
                $stmtA_bucle = $this->conn->prepare($qActividad);
                $stmtA_bucle->execute([':u' => $user_id, ':f' => $fecha_completa]);
                $quemadas_dia = $stmtA_bucle->fetch(PDO::FETCH_ASSOC)['total'];

                $historial_semanal[] = [
                    "fecha" => $fecha_completa, // Enviamos la fecha completa a Android
                    "consumidas" => (int)$cals_dia,
                    "quemadas" => (int)$quemadas_dia
                ];
            }

            // 3. Empaquetar todo en JSON para Android
            http_response_code(200);
            echo json_encode([
                "consumidas" => (int)$consumidas,
                "quemadas" => (int)$quemadas,
                "netas" => (int)($consumidas - $quemadas),
                "historial_semanal" => $historial_semanal
            ]);

        } catch (PDOException $e) {
            http_response_code(500);
            echo json_encode(["error" => "Error de BD: " . $e->getMessage()]);
        }
    }
}
?>