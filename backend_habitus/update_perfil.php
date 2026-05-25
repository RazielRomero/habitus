<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

require_once 'config/Database.php';

$data = json_decode(file_get_contents("php://input"));

if (isset($data->id) && isset($data->name)) {
    try {
        $database = new Database();
        $db = $database->getConnection();

        $query = "UPDATE users 
                  SET name = :name, email = :email, gender = :gender, 
                      birth_date = :dob, height_cm = :height, weight_kg = :weight 
                  WHERE id = :id";
                  
        $stmt = $db->prepare($query);
        
        $stmt->execute([
            ':name' => $data->name,
            ':email' => $data->email,
            ':gender' => $data->gender,
            ':dob' => $data->birth_date,
            ':height' => $data->height_cm,
            ':weight' => $data->weight_kg,
            ':id' => $data->id
        ]);

        http_response_code(200);
        echo json_encode(["mensaje" => "¡Perfil actualizado exitosamente!"]);

    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(["error" => "Error al actualizar: " . $e->getMessage()]);
    }
} else {
    http_response_code(400);
    echo json_encode(["error" => "Datos incompletos"]);
}
?>