<?php
class CatalogController {
    private $conn;

    public function __construct($db) {
        $this->conn = $db;
    }

    public function getCatalogos() {
        try {
            // 1. Consultar alimentos
            $queryFoods = "SELECT id, name, calories_per_unit, unit FROM foods";
            $stmtFoods = $this->conn->query($queryFoods);
            $foods = $stmtFoods->fetchAll(PDO::FETCH_ASSOC);

            // 2. Consultar actividades
            $queryActivities = "SELECT id, name, calories_per_minute FROM activities";
            $stmtActivities = $this->conn->query($queryActivities);
            $activities = $stmtActivities->fetchAll(PDO::FETCH_ASSOC);

            // 3. Devolver JSON exitoso
            http_response_code(200);
            echo json_encode([
                "foods" => $foods,
                "activities" => $activities
            ]);

        } catch (PDOException $e) {
            http_response_code(500);
            echo json_encode(["error" => "Error de BD: " . $e->getMessage()]);
        }
    }
}
?>