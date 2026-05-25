<?php
date_default_timezone_set('America/Mexico_City');

class Database {
    private $host;
    private $db_name;
    private $username;
    private $password; 
    public $conn;

    public function __construct() {
        $this->host = getenv('DB_HOST') ?: "localhost";
        $this->db_name = getenv('DB_NAME') ?: "backend";
        $this->username = getenv('DB_USER') ?: "root";
        $this->password = getenv('DB_PASSWORD') !== false ? getenv('DB_PASSWORD') : "";
    }

    public function getConnection() {
        $this->conn = null;
        try {
            $this->conn = new PDO("mysql:host=" . $this->host . ";dbname=" . $this->db_name, $this->username, $this->password);
            
            // Configuración de caracteres
            $this->conn->exec("set names utf8");
            
            // Forzamos a MySQL a usar la hora de México en esta conexión
            $this->conn->exec("SET time_zone = '-06:00'");
            
        } catch(PDOException $exception) {
            echo json_encode(["error" => "Error de conexión: " . $exception->getMessage()]);
            exit();
        }
        return $this->conn;
    }
}
?>