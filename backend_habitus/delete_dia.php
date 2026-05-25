<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, GET");

require_once 'config/Database.php';

$data = json_decode(file_get_contents("php://input"));

// Atrapamos los datos: ya sea que vengan de Android (POST JSON) o de Chrome (GET)
$user_id = isset($_GET['user_id']) ? $_GET['user_id'] : (isset($data->user_id) ? $data->user_id : null);
$fecha = isset($_GET['fecha']) ? $_GET['fecha'] : (isset($data->fecha) ? $data->fecha : null);

if ($user_id !== null && $fecha !== null) {
    $database = new Database();
    $db = $database->getConnection();

    try {
        $db->beginTransaction();

        $q1 = "DELETE FROM food_logs WHERE user_id = :u AND record_date = :f";
        $stmt1 = $db->prepare($q1);
        $stmt1->execute([':u' => $user_id, ':f' => $fecha]);

        $q2 = "DELETE FROM activity_logs WHERE user_id = :u AND record_date = :f";
        $stmt2 = $db->prepare($q2);
        $stmt2->execute([':u' => $user_id, ':f' => $fecha]);

        $q3 = "DELETE FROM energy_balance WHERE user_id = :u AND record_date = :f";
        $stmt3 = $db->prepare($q3);
        $stmt3->execute([':u' => $user_id, ':f' => $fecha]);

        $db->commit();

        http_response_code(200);
        echo json_encode(["mensaje" => "¡Exito! Se borró todo el registro del día: " . $fecha]);

    } catch (PDOException $e) {
        $db->rollBack();
        http_response_code(500);
        echo json_encode(["error" => "Error de BD: " . $e->getMessage()]);
    }
} else {
    http_response_code(400);
    echo json_encode(["error" => "Faltan datos para eliminar (user_id o fecha)"]);
}
?>