<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

require_once 'config/Database.php';

if (isset($_GET['user_id'])) {
    try {
        $database = new Database();
        $db = $database->getConnection();

        $query = "SELECT id, name, email, gender, birth_date, height_cm, weight_kg FROM users WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->execute([':id' => $_GET['user_id']]);
        
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($user) {
            $user['id'] = (int)$user['id'];
            $user['name'] = $user['name'] ?? "";
            $user['email'] = $user['email'] ?? "";
            $user['gender'] = $user['gender'] ?? "";
            $user['birth_date'] = $user['birth_date'] ?? "";
            $user['height_cm'] = isset($user['height_cm']) ? (string)$user['height_cm'] : "";
            $user['weight_kg'] = isset($user['weight_kg']) ? (string)$user['weight_kg'] : "";
            
            http_response_code(200);
            echo json_encode($user);
        } else {
            http_response_code(404);
            echo json_encode(["error" => "Usuario no encontrado"]);
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(["error" => "Error de BD: " . $e->getMessage()]);
    }
} else {
    http_response_code(400);
    echo json_encode(["error" => "Falta el user_id"]);
}
?>