<?php
require_once('config_keys.php');

class GeminiService {
    private $api_key = GEMINI_API_KEY; 
    private $model = "gemini-2.5-flash"; 

    public function obtenerRecomendacion($alimentos, $actividades, $calIn, $calOut, $balance, $bmi, $status) {
        $url = "https://generativelanguage.googleapis.com/v1beta/models/" . $this->model . ":generateContent?key=" . $this->api_key;

        $prompt = "Eres el coach de nutrición de la app Habitus. El usuario acaba de hacer un registro individual (NO es el final de su día, es solo una comida o sesión de ejercicio). 
        Datos de ESTE registro exacto:
        - IMC: $bmi
        - Impacto calórico de esta acción: $balance kcal ($status)
        - Alimentos que acaba de ingresar: $alimentos ($calIn kcal)
        - Ejercicio que acaba de hacer: $actividades ($calOut kcal)
        
        Escribe un consejo rápido y motivacional de 2 o 3 oraciones evaluando ESTA decisión específica. Dale una sugerencia sobre cómo balancear el RESTO de su día (ej. 'para tu próxima comida', 'más tarde'). 
        REGLA ESTRICTA: NO hables como si el día hubiera terminado. NO uses frases como 'tu día fue', 'para mañana' ni te despidas. El día sigue en curso. No uses viñetas ni asteriscos.";

        $data = [
            "contents" => [
                [
                    "parts" => [
                        ["text" => $prompt]
                    ]
                ]
            ],
            "generationConfig" => [
                "temperature" => 0.7 
            ]
        ];

        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json'
        ]);
        
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        
        // Le damos un límite a PHP para que tampoco se quede esperando eternamente
        curl_setopt($ch, CURLOPT_TIMEOUT, 10); 

        $response = curl_exec($ch);

        // Si la IA falla por red o tiempo, regresamos un mensaje limpio y rápido
        if (curl_errno($ch)) {
            curl_close($ch);
            return "Día registrado con éxito. Mantén el buen ritmo en tus hábitos.";
        }

        curl_close($ch);
        $jsonDecoded = json_decode($response, true);

        if (isset($jsonDecoded['candidates'][0]['content']['parts'][0]['text'])) {
            // Limpiamos espacios extra en la respuesta
            return trim($jsonDecoded['candidates'][0]['content']['parts'][0]['text']);
        }

        return "Registro guardado. Sigue esmerándote en tu plan diario.";
    }
}
?>