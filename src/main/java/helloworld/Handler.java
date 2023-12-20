package helloworld;

import java.util.Map;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        Map<String, String> queryStringParameters = input.getQueryStringParameters();
        if (queryStringParameters == null || !queryStringParameters.containsKey("dataParametro")) {
            return createErrorResponse(400, "Missing 'dataParametro' in query parameters");
        }

        String dataParametro = queryStringParameters.get("dataParametro");

        if (dataParametro == null || dataParametro.trim().isEmpty()) {
            return createErrorResponse(400, "DataParametro is null or empty");
        }

        LocalDate data = null;

        try {
            if (LocalDate.parse(dataParametro, DateTimeFormatter.ofPattern("dd/MM/yyyy")).equals(LocalDate.now())) {
                verificarDataHoje(LocalDate.now(), context);
                procurarPalindromo(LocalDate.now(), context);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                data = LocalDate.parse(dataParametro, formatter);

                dataHojePalindromo(data, context);
                procurarPalindromo(data, context);
            }
        } catch (Exception e) {
            context.getLogger().log("Error parsing date: " + e.getMessage());
            return createErrorResponse(400, "Error parsing date: " + e.getMessage());
        }

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setStatusCode(200);
        responseEvent.setBody("Success - Palindrome Check: " + verificaPalindromo(dataParametro));

        return responseEvent;
    }

    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setStatusCode(statusCode);
        responseEvent.setBody(message);
        return responseEvent;
    }

    public static void verificarDataHoje(LocalDate dataHoje, Context context) {
        // Chamando a função de verificação
        dataHojePalindromo(dataHoje, context);
    }

    public static void dataHojePalindromo(LocalDate data, Context context) {
        DateTimeFormatter formatoBrasileiro = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataFormatada = data.format(formatoBrasileiro);

        context.getLogger().log("A data: " + dataFormatada + " !! ");

        if (verificaPalindromo(dataFormatada)) {
            context.getLogger().log("é um palíndromo ");
        } else {
            context.getLogger().log("não é um palíndromo ");
        }
    }

    public static void procurarPalindromo(LocalDate data, Context context) {
        DateTimeFormatter formatoBrasileiro = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataTemp = data.format(formatoBrasileiro);

        if (verificaPalindromo(dataTemp)) {
            context.getLogger().log("A próxima data em palíndromo será: " + dataTemp);
        } else {
            LocalDate buscaPalindromo = data.plusDays(1);
            procurarPalindromo(buscaPalindromo, context);
        }
    }

    // Função de verificação de palíndromo adicionada
    public static boolean verificaPalindromo(String dataFormatada) {
        String dataSemBarras = dataFormatada.replaceAll("[-/]", "");
        String dataInvertida = inverterString(dataSemBarras);
        return dataSemBarras.equals(dataInvertida);
    }

    // Função de inversão de string adicionada
    public static String inverterString(String str) {
        return new StringBuilder(str).reverse().toString();
    }
}