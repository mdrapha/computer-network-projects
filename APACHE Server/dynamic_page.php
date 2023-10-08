<!DOCTYPE html>
<html>
<head>
    <title>Conteúdo Dinâmico</title>
    <style>
        body {
            background-color: black;
            color: white;
            text-align: center;
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
        }

        h1 {
            margin-top: 20px;
        }

        p {
            font-size: 18px;
        }

        span {
            font-weight: bold;
        }
    </style>
</head>
<body>
    <h1>Conteúdo Dinâmico</h1>
    <p>IP do Cliente: <?php echo $_SERVER['REMOTE_ADDR']; ?></p>
    <p>Data e Hora do Servidor: <span id="data-hora"></span></p>
    <p>Local: <span id="local"></span></p>

    <script>
        // Função para atualizar a data e a hora
        function atualizarDataHora() {
            var agora = new Date();
            var dataHora = agora.toLocaleString();
            document.getElementById('data-hora').textContent = dataHora;
        }

        // Função para obter o local do cliente usando a API Geolocation
        function obterLocal() {
            if ("geolocation" in navigator) {
                navigator.geolocation.getCurrentPosition(function(position) {
                    var latitude = position.coords.latitude;
                    var longitude = position.coords.longitude;
                    document.getElementById('local').textContent = "Latitude: " + latitude + ", Longitude: " + longitude;
                });
            } else {
                document.getElementById('local').textContent = "Geolocalização não suportada pelo seu navegador.";
            }
        }

        // Chamar as funções para atualizar o conteúdo a cada 5 segundos
        setInterval(atualizarDataHora, 5000); // 5000 milissegundos = 5 segundos
        setInterval(obterLocal, 5000); // 5000 milissegundos = 5 segundos

        // Chamar as funções imediatamente para exibir o conteúdo inicial
        atualizarDataHora();
        obterLocal();
    </script>
</body>
</html>
