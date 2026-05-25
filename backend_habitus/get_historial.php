<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

require_once 'config/Database.php';
require_once 'controllers/HistorialController.php';

// Verificamos que nos hayan mandado el ID del usuario
if (isset($_GET['user_id'])) {
    $database = new Database();
    $db = $database->getConnection();

    $controller = new HistorialController($db);
    $controller->getResumenHoy($_GET['user_id']);
} else {
    http_response_code(400);
    echo json_encode(["error" => "Falta el user_id en la petición"]);
}
?>