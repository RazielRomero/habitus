<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

// 1. Requerir tus archivos directamente desde las carpetas
require_once 'config/Database.php'; 
require_once 'controllers/CatalogController.php';

// 2. Instanciar la base de datos
$database = new Database(); 
$db = $database->getConnection();

// 3. Instanciar el controlador y ejecutar
$catalogController = new CatalogController($db);
$catalogController->getCatalogos();
?>