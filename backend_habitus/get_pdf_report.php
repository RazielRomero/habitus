<?php
require('fpdf.php');
require('config/database.php'); 

// Forzar zona horaria de México
date_default_timezone_set('America/Mexico_City');

$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;
$dias = isset($_GET['dias']) ? intval($_GET['dias']) : 7;

// 1. Obtener los datos de la Base de Datos utilizando tu conexión PDO
$database = new Database();
$db = $database->getConnection();

// CONSULTA 1: Obtener datos del perfil del usuario
$queryUsuario = "SELECT name, birth_date, height_cm, weight_kg FROM users WHERE id = :user_id";
$stmtUser = $db->prepare($queryUsuario);
$stmtUser->bindParam(":user_id", $user_id, PDO::PARAM_INT);
$stmtUser->execute();
$usuario = $stmtUser->fetch(PDO::FETCH_ASSOC);

// Cálculo matemático de Edad e IMC basados en el perfil obtenido de la db
$edad = 0;
$imc = 0;
if ($usuario) {
    $fechaNacimiento = new DateTime($usuario['birth_date']);
    $hoy = new DateTime();
    $edad = $hoy->diff($fechaNacimiento)->y;

    $estaturaMetros = $usuario['height_cm'] / 100;
    if ($estaturaMetros > 0) {
        $imc = round($usuario['weight_kg'] / ($estaturaMetros * $estaturaMetros), 1);
    }
}

// CONSULTA 2: Traer el historial de energía diaria
$queryHistorial = "SELECT record_date, calories_in, calories_out 
          FROM energy_balance 
          WHERE user_id = :user_id 
          AND record_date >= DATE_SUB(CURDATE(), INTERVAL :dias DAY) 
          ORDER BY record_date DESC";

$stmtHistorial = $db->prepare($queryHistorial);
$stmtHistorial->bindParam(":user_id", $user_id, PDO::PARAM_INT);
$stmtHistorial->bindParam(":dias", $dias, PDO::PARAM_INT);
$stmtHistorial->execute();
$registros = $stmtHistorial->fetchAll(PDO::FETCH_ASSOC);

// INICIA CREACIÓN DEL PDF

$pdf = new FPDF();
$pdf->AddPage();

// ENCABEZADO INSTITUCIONAL
$pdf->SetFont('Arial', 'B', 16);
$pdf->Cell(0, 10, 'HABITUS - REPORTE DE EVOLUCION', 0, 1, 'C');
$pdf->SetFont('Arial', 'I', 10);
$pdf->Cell(0, 6, 'Reporte integral generado el ' . date('d-m-Y'), 0, 1, 'C');
$pdf->Ln(6);

// SECCIÓN: FICHA DE DATOS DEL USUARIO
if ($usuario) {
    $pdf->SetDrawColor(25, 118, 210); // Azul institucional Habitus
    $pdf->SetLineWidth(0.4);
    
    // Guardamos la posición exacta del inicio del bloque del recuadro
    $yInicioBloque = $pdf->GetY();
    
    // Título del recuadro con margen interno (X=13)
    $pdf->SetY($yInicioBloque + 4); 
    $pdf->SetX(13); 
    $pdf->SetFont('Arial', 'B', 11);
    $pdf->SetTextColor(25, 118, 210);
    $pdf->Cell(0, 5, utf8_decode('Datos del perfil'), 0, 1, 'L');
    $pdf->Ln(3); 
    
    $pdf->SetTextColor(0, 0, 0);
    
    // Fila 1: Nombre y Edad con margen interno (X=13)
    $pdf->SetX(13); 
    $pdf->SetFont('Arial', 'B', 10); $pdf->Cell(18, 6, 'Nombre: ', 0, 0, 'L');
    $pdf->SetFont('Arial', '', 10);  $pdf->Cell(85, 6, utf8_decode($usuario['name']), 0, 0, 'L');
    $pdf->SetFont('Arial', 'B', 10); $pdf->Cell(15, 6, 'Edad: ', 0, 0, 'L');
    $pdf->SetFont('Arial', '', 10);  $pdf->Cell(40, 6, $edad . utf8_decode(' años'), 0, 1, 'L');
    
    // Fila 2: Estatura y Peso 
    $pdf->SetX(13);
    $pdf->SetFont('Arial', 'B', 10); $pdf->Cell(18, 6, 'Estatura: ', 0, 0, 'L');
    $pdf->SetFont('Arial', '', 10);  $pdf->Cell(85, 6, intval($usuario['height_cm']) . ' cm', 0, 0, 'L');
    $pdf->SetFont('Arial', 'B', 10); $pdf->Cell(15, 6, 'Peso: ', 0, 0, 'L');
    $pdf->SetFont('Arial', '', 10);  $pdf->Cell(40, 6, floatval($usuario['weight_kg']) . ' kg', 0, 1, 'L');
    
    // Fila 3: Índice de Masa Corporal (IMC)
    $pdf->SetX(13);
    $pdf->SetFont('Arial', 'B', 10); $pdf->Cell(18, 6, 'IMC: ', 0, 0, 'L');
    $pdf->SetFont('Arial', '', 10);  $pdf->Cell(85, 6, $imc, 0, 1, 'L');
    
    $yFinContenido = $pdf->GetY();
    $yFinRecuadro = $yFinContenido + 3;
    $dynamicHeight = $yFinRecuadro - $yInicioBloque;
    
    // El rectángulo empieza en X=10 y mide 180 de ancho.
    $pdf->Rect(10, $yInicioBloque, 180, $dynamicHeight);
    
    // Continuar por debajo del recuadro
    $pdf->SetY($yFinRecuadro + 7); 
}

// TABLA DE DATOS DEL HISTORIAL 
$pdf->SetFont('Arial', 'B', 12);
$pdf->Cell(0, 8, 'Historial de balances (Ultimos ' . $dias . ' dias)', 0, 1, 'L');
$pdf->Ln(2);

$pdf->SetFont('Arial', 'B', 11);
$pdf->SetFillColor(25, 118, 210); 
$pdf->SetTextColor(255, 255, 255); 

// La tabla, por defecto, se dibuja desde X=10. Las 4 celdas miden 45 (45*4 = 180). 
$pdf->Cell(45, 8, 'Fecha', 1, 0, 'C', true);
$pdf->Cell(45, 8, 'Consumido', 1, 0, 'C', true);
$pdf->Cell(45, 8, 'Quemado', 1, 0, 'C', true);
$pdf->Cell(45, 8, 'Balance Neto', 1, 1, 'C', true);

$pdf->SetFont('Arial', '', 10);
$pdf->SetTextColor(0, 0, 0);

foreach ($registros as $row) {
    $balance = $row['calories_in'] - $row['calories_out'];
    $pdf->Cell(45, 7, $row['record_date'], 1, 0, 'C');
    $pdf->Cell(45, 7, $row['calories_in'] . ' kcal', 1, 0, 'C');
    $pdf->Cell(45, 7, $row['calories_out'] . ' kcal', 1, 0, 'C');
    $pdf->Cell(45, 7, $balance . ' kcal', 1, 1, 'C');
}

// PÁGINA 2: LA GRÁFICA VISUAL 
if (count($registros) > 0) {
    $pdf->AddPage(); 
    
    $pdf->SetFont('Arial', 'B', 14);
    $pdf->Cell(0, 10, 'Grafica: Calorias en los ultimos ' . $dias . ' dias', 0, 1, 'C');
    $pdf->Ln(10);

    $max_cal = 0;
    foreach ($registros as $row) {
        if ($row['calories_in'] > $max_cal) $max_cal = $row['calories_in'];
        if ($row['calories_out'] > $max_cal) $max_cal = $row['calories_out'];
    }
    if ($max_cal == 0) $max_cal = 2000; 

    $chartX = 20; 
    $chartY = $pdf->GetY(); 
    $chartWidth = 170; 
    $chartHeight = 80; 

    $pdf->SetDrawColor(150, 150, 150);
    $pdf->Line($chartX, $chartY, $chartX, $chartY + $chartHeight); 
    $pdf->Line($chartX, $chartY + $chartHeight, $chartX + $chartWidth, $chartY + $chartHeight); 

    $datosGrafica = array_reverse($registros);
    $numDias = count($datosGrafica);
    $espacioPorDia = $chartWidth / $numDias;
    $anchoBarra = $espacioPorDia / 3; 
    
    $posX = $chartX + 2; 

    if ($numDias > 15) {
        $pdf->SetFont('Arial', '', 6);
    } else {
        $pdf->SetFont('Arial', '', 8);
    }

    foreach ($datosGrafica as $row) {
        $alturaConsumo = ($row['calories_in'] / $max_cal) * $chartHeight;
        $alturaQuema = ($row['calories_out'] / $max_cal) * $chartHeight;

        $pdf->SetFillColor(25, 118, 210); 
        $pdf->Rect($posX, $chartY + $chartHeight - $alturaConsumo, $anchoBarra, $alturaConsumo, 'F');

        $pdf->SetFillColor(245, 124, 0); 
        $pdf->Rect($posX + $anchoBarra, $chartY + $chartHeight - $alturaQuema, $anchoBarra, $alturaQuema, 'F');

        $pdf->SetTextColor(100, 100, 100);
        $fechaCorta = date('d-m', strtotime($row['record_date'])); 
        $centroGrupo = $posX + $anchoBarra; 
        
        $desplazamientoTexto = ($numDias > 15) ? 2.5 : 3.5;
        $pdf->Text($centroGrupo - $desplazamientoTexto, $chartY + $chartHeight + 5, $fechaCorta);

        $posX += $espacioPorDia;
    }

    $pdf->SetY($chartY + $chartHeight + 15);
    $pdf->SetFont('Arial', '', 10);
    $pdf->SetTextColor(0, 0, 0);
    
    $pdf->SetFillColor(25, 118, 210);
    $pdf->Rect(65, $pdf->GetY(), 4, 4, 'F');
    $pdf->Text(71, $pdf->GetY() + 4, 'Consumido');

    $pdf->SetFillColor(245, 124, 0);
    $pdf->Rect(105, $pdf->GetY(), 4, 4, 'F');
    $pdf->Text(111, $pdf->GetY() + 4, 'Quemado');
}

ob_end_clean(); 
$pdf->Output('I', 'Reporte_Habitus.pdf');
?>