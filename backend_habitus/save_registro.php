<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

require_once 'config/Database.php';
require_once 'controllers/RegistroController.php';

$database = new Database();
$db = $database->getConnection();

// Recibimos el JSON que manda Kotlin
$data = json_decode(file_get_contents("php://input"));

if(!empty($data->user_id)) {
    // CAMBIO 2: Instanciamos nuestra clase maestra
    $controller = new RegistroController($db);
    
    // CAMBIO 3: Llamamos a la función que tiene a Gemini
    $controller->guardarRegistroCompleto($data);
} else {
    http_response_code(400);
    echo json_encode(["error" => "Datos incompletos"]);
}
?>