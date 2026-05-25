<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

require_once 'config/Database.php';

// Atrapamos los datos JSON que nos manda Kotlin
$data = json_decode(file_get_contents("php://input"));

if (!empty($data->id_registro) && !empty($data->tipo)) {
    $database = new Database();
    $db = $database->getConnection();

    try {
        // Dependiendo de la palabra que nos manden, borramos en una tabla o en la otra
        if ($data->tipo === 'comida') {
            $query = "DELETE FROM food_logs WHERE id = :id";
        } else if ($data->tipo === 'ejercicio') {
            $query = "DELETE FROM activity_logs WHERE id = :id";
        } else {
            http_response_code(400);
            echo json_encode(["error" => "Tipo de registro no válido"]);
            exit;
        }

        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id_registro);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["mensaje" => "Registro eliminado correctamente."]);
        } else {
            http_response_code(503);
            echo json_encode(["error" => "No se pudo eliminar el registro."]);
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(["error" => "Error de BD: " . $e->getMessage()]);
    }
} else {
    http_response_code(400);
    echo json_encode(["error" => "Faltan datos para eliminar (id_registro o tipo)"]);
}
?>