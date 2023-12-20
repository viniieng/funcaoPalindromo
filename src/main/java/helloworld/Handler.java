package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;


public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public static String procurarPalindromo(LocalDate data, Context context) {
        DateTimeFormatter formatoBrasileiro = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataTemp = data.format(formatoBrasileiro);

        if (verificaPalindromo(dataTemp)) {
            context.getLogger().log("A próxima data em palíndromo será: " + dataTemp);
            return dataTemp;
        } else {
            LocalDate buscaPalindromo = data.plusDays(1);
            return procurarPalindromo(buscaPalindromo, context);

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

    public boolean validadeData(String strDate) {

        String dateFormat = "dd/MM/uuuu";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern(dateFormat)
                .withResolverStyle(ResolverStyle.STRICT);
        try {
            LocalDate date = LocalDate.parse(strDate, dateTimeFormatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

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
        if (!validadeData(dataParametro)) {
            return createErrorResponse(400, "DataParametro is invalid");
        }


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data = LocalDate.parse(dataParametro, formatter);

        try {
            APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
            responseEvent.setStatusCode(200);
            boolean isPalindromo = verificaPalindromo(dataParametro);
            if (isPalindromo) {
                responseEvent.setBody("A data " + dataParametro + " é um palíndromo");
            } else {
                responseEvent.setBody("A data " + dataParametro + " não é um palíndromo. " +
                        "A próxima data palíndromo é: " + procurarPalindromo(data, context));
            }
            return responseEvent;
        } catch (Exception e) {
            context.getLogger().log("Error parsing date: " + e.getMessage());
            return createErrorResponse(400, "Error parsing date: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setStatusCode(statusCode);
        responseEvent.setBody(message);
        return responseEvent;
    }
}