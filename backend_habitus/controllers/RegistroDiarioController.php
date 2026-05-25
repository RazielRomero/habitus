<?php
class RegistroDiarioController {
    private $conn;

    public function __construct($db) {
        $this->conn = $db;
    }

    public function guardar($data) {
        try {
            // Empezamos una "transacción" para que todo se guarde junto
            $this->conn->beginTransaction();

            $userId = $data->user_id;
            $date = date('Y-m-d'); // Obtenemos la fecha de hoy

            // 1. Guardar alimentos en food_logs
            if (!empty($data->foods)) {
                $stmtFood = $this->conn->prepare("INSERT INTO food_logs (user_id, record_date, food_id, quantity) VALUES (?, ?, ?, ?)");
                foreach ($data->foods as $food) {
                    $stmtFood->execute([$userId, $date, $food->id, $food->cantidad]);
                }
            }

            // 2. Guardar actividades en activity_logs
            if (!empty($data->activities)) {
                $stmtAct = $this->conn->prepare("INSERT INTO activity_logs (user_id, record_date, activity_id, minutes) VALUES (?, ?, ?, ?)");
                foreach ($data->activities as $act) {
                    $stmtAct->execute([$userId, $date, $act->id, $act->minutos]);
                }
            }

            // confirmamos los cambios
            $this->conn->commit();
            http_response_code(200);
            echo json_encode(["mensaje" => "¡Registros guardados correctamente en la base de datos!"]);

        } catch (Exception $e) {
            // Si algo falla, revertimos para no dejar datos a medias
            $this->conn->rollBack();
            http_response_code(500);
            echo json_encode(["error" => "Error al guardar: " . $e->getMessage()]);
        }
    }
}
?>