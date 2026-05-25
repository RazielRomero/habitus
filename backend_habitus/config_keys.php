<?php
// Configuración dinámica a través de variables de entorno para Docker
define('GEMINI_API_KEY', getenv('GEMINI_API_KEY') ?: 'TU_API_KEY_AQUI');
define('JWT_SECRET_KEY', getenv('JWT_SECRET_KEY') ?: 'Tu_SECRET_KEY');
?>
