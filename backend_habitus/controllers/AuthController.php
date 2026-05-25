<?php
require_once __DIR__ . '/../vendor/autoload.php';
require_once 'config_keys.php'; 

use \Firebase\JWT\JWT;

class AuthController {
    private $conn;
    private $secret_key = JWT_SECRET_KEY;

    public function __construct($db) {
        $this->conn = $db;
    }

   public function register($data) {
        try {
            // 1. Verificar si el correo ya existe
            $queryCheck = "SELECT id FROM users WHERE email = :email LIMIT 1";
            $stmtCheck = $this->conn->prepare($queryCheck);
            $stmtCheck->execute([':email' => $data->email]);

            // Si fetch() devuelve datos, significa que el correo ya está ocupado
            if ($stmtCheck->fetch(PDO::FETCH_ASSOC)) {
                http_response_code(400); // 400 = Bad Request / Error
                echo json_encode(["error" => "Este correo electrónico ya está registrado, intente con otro"]);
                return; // El 'return' detiene la función aquí para que no haga el INSERT
            }

            // 2. Si el correo está libre, procedemos con el registro normal
            $password_encriptada = password_hash($data->password, PASSWORD_BCRYPT);

            $query = "INSERT INTO users (name, email, password, gender, birth_date, height_cm, weight_kg) 
                      VALUES (:name, :email, :password, :gender, :dob, :height, :weight)";
            
            $stmt = $this->conn->prepare($query);
            $stmt->execute([
                ':name' => $data->name,
                ':email' => $data->email,
                ':password' => $password_encriptada,
                ':gender' => $data->gender,
                ':dob' => $data->date_of_birth, 
                ':height' => $data->height_cm,
                ':weight' => $data->weight_kg
            ]);

            http_response_code(201);
            echo json_encode(["mensaje" => "Usuario registrado exitosamente"]);
        } catch (PDOException $e) {
            http_response_code(400);
            echo json_encode(["error" => "Error al registrar: " . $e->getMessage()]);
        }
    }

    public function login($data) {
        $query = "SELECT id, name, email, password, height_cm, weight_kg FROM users WHERE email = :email LIMIT 1";
        $stmt = $this->conn->prepare($query);
        $stmt->execute([':email' => $data->email]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if($user && password_verify($data->password, $user['password'])) {
            $token = [
                "iat" => time(),
                "exp" => time() + (60 * 60 * 24),
                "data" => [
                    "id" => $user['id'],
                    "name" => $user['name'],
                    "email" => $user['email']
                ]
            ];
            
            $jwt = JWT::encode($token, $this->secret_key, 'HS256');
            
            $altura_m = $user['height_cm'] / 100;
            $imc = $user['weight_kg'] / ($altura_m * $altura_m);

            http_response_code(200);
            echo json_encode([
                "mensaje" => "Login exitoso", 
                "id" => $user['id'],
                "token" => $jwt, 
                "imc_actual" => round($imc, 2),
                "usuario" => $user['name']
            ]);
        } else {
            http_response_code(401);
            echo json_encode(["error" => "Credenciales incorrectas"]);
        }
    }
}
?>