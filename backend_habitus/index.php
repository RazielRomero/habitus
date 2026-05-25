<?php
// Mostrar errores para facilitar la depuración
ini_set('display_errors', 1);
error_reporting(E_ALL);

// Cabeceras de seguridad y CORS
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Manejar la petición OPTIONS "pre-flight" de navegadores/Postman
if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Incluir archivos necesarios
require_once 'config/Database.php';
require_once 'controllers/AuthController.php';
require_once 'controllers/RegistroController.php';

// Conectar a la base de datos
$database = new Database();
$db = $database->getConnection();

// Lógica inteligente para obtener el endpoint (siempre lee la última palabra de la URL)
$ruta = trim(parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH), '/');
$partes = explode('/', $ruta);
$endpoint = end($partes);
// Decodificar la URL y quitar cualquier caracter que no sea letra o guión bajo
$endpoint = preg_replace('/[^a-zA-Z0-9_]/', '', urldecode($endpoint));

// Leer los datos JSON del cuerpo de la petición
$data = json_decode(file_get_contents("php://input"));

// Evaluar la ruta y llamar al controlador correspondiente
switch ($endpoint) {
    case 'register':
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            // Instanciamos el controlador de autenticación
            $auth = new AuthController($db);
            // Llamamos a la función de registro
            $auth->register($data); 
        }
        break;

    case 'login':
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $auth = new AuthController($db);
            $auth->login($data);
        }
        break;

    case 'alimentos':
        if ($_SERVER['REQUEST_METHOD'] === 'GET') {
            $registro = new RegistroController($db);
            $registro->obtenerAlimentos();
        }
        break;

    case 'actividades':
        if ($_SERVER['REQUEST_METHOD'] === 'GET') {
            $registro = new RegistroController($db);
            $registro->obtenerActividades();
        }
        break;

    case 'guardar_balance':
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $registro = new RegistroController($db);
            $registro->guardarBalanceDiario($data);
        }
        break;
    case 'guardar_dia_completo':
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $registro = new RegistroController($db);
            $registro->guardarRegistroCompleto($data);
        }
        break;

    default:
        http_response_code(404);
        echo json_encode(["error" => "Endpoint no existe. Endpoint leído: " . $endpoint]);
        break;
}
?>