<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

require_once 'config/Database.php';

if (isset($_GET['user_id'])) {
    $database = new Database();
    $db = $database->getConnection();
    
    $user_id = $_GET['user_id'];
    // Ahora atrapamos los días (7, 15 o 30)
    $dias_limite = isset($_GET['dias']) ? (int)$_GET['dias'] : 7;
    // Calculamos hasta qué fecha en el pasado vamos a buscar
    $fecha_limite = date('Y-m-d', strtotime("-$dias_limite days"));

    try {
        $registros = [];

        // 1. Buscamos comidas desde la fecha límite hasta hoy
        $qComidas = "SELECT fl.id, f.name as nombre, fl.quantity as cantidad, 
                            (fl.quantity * f.calories_per_unit) as calorias_totales, 
                            DATE(fl.record_date) as fecha,
                            TIME(fl.record_date) as hora, 'comida' as tipo 
                     FROM food_logs fl 
                     JOIN foods f ON fl.food_id = f.id 
                     WHERE fl.user_id = :u AND DATE(fl.record_date) >= :f";
        
        $stmtC = $db->prepare($qComidas);
        $stmtC->execute([':u' => $user_id, ':f' => $fecha_limite]);
        
        while ($row = $stmtC->fetch(PDO::FETCH_ASSOC)) {
            $registros[] = [
                "id_registro" => (int)$row['id'],
                "nombre" => $row['nombre'],
                "cantidad" => (int)$row['cantidad'],
                "calorias" => (int)$row['calorias_totales'],
                "fecha" => $row['fecha'], // Agregamos la fecha
                "hora" => date('H:i', strtotime($row['hora'])),
                "tipo" => $row['tipo']
            ];
        }

        // 2. Buscamos actividades desde la fecha límite hasta hoy
        $qActividades = "SELECT al.id, a.name as nombre, al.minutes as cantidad, 
                                (al.minutes * a.calories_per_minute) as calorias_totales, 
                                DATE(al.record_date) as fecha,
                                TIME(al.record_date) as hora, 'ejercicio' as tipo 
                         FROM activity_logs al 
                         JOIN activities a ON al.activity_id = a.id 
                         WHERE al.user_id = :u AND DATE(al.record_date) >= :f";
                         
        $stmtA = $db->prepare($qActividades);
        $stmtA->execute([':u' => $user_id, ':f' => $fecha_limite]);
        
        while ($row = $stmtA->fetch(PDO::FETCH_ASSOC)) {
            $registros[] = [
                "id_registro" => (int)$row['id'],
                "nombre" => $row['nombre'],
                "cantidad" => (int)$row['cantidad'],
                "calorias" => (int)$row['calorias_totales'],
                "fecha" => $row['fecha'], // Agregamos la fecha
                "hora" => date('H:i', strtotime($row['hora'])),
                "tipo" => $row['tipo']
            ];
        }

        // 3. Ordenamos: los más recientes hasta arriba
        usort($registros, function($a, $b) {
            $timeA = strtotime($a['fecha'] . ' ' . $a['hora']);
            $timeB = strtotime($b['fecha'] . ' ' . $b['hora']);
            return $timeB - $timeA; 
        });

        http_response_code(200);
        echo json_encode(["registros" => $registros]);

    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(["error" => "Error de BD: " . $e->getMessage()]);
    }
} else {
    http_response_code(400);
    echo json_encode(["error" => "Falta el user_id"]);
}
?>