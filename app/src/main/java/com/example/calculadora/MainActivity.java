package com.example.calculadora;

import androidx.appcompat.app.AlertDialog; // NOVO: Import para AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter; // NOVO: Import para ArrayAdapter
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale; // NOVO: Import para formatação de número

public class MainActivity extends AppCompatActivity {

    private TextView textViewResultado;
    private TextView textViewHistorico;

    private StringBuilder currentNumber = new StringBuilder("0");
    private StringBuilder equacaoDisplay = new StringBuilder();
    private ArrayList<String> historicoCompleto = new ArrayList<>();

    private double numero1 = 0;
    private String operacaoPendente = null;
    private boolean novaOperacao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewResultado = findViewById(R.id.text_view_resultado);
        textViewHistorico = findViewById(R.id.text_view_historico);
        atualizarDisplay();
    }

    // Inserir números e ponto
    public void inserirNumero(View view) {
        String valor = ((android.widget.Button) view).getText().toString();

        if (novaOperacao) {
            currentNumber.setLength(0);
            novaOperacao = false;
        }

        if (currentNumber.toString().equals("0") && !valor.equals(".")) {
            currentNumber.setLength(0);
        }

        if (valor.equals(".") && currentNumber.toString().contains(".")) return;

        currentNumber.append(valor);
        atualizarDisplay();
    }

    // Selecionar operação (+, -, *, /)
    public void calcular(View view) {
        String operacao = ((android.widget.Button) view).getText().toString();

        try {
            numero1 = Double.parseDouble(currentNumber.toString());
        } catch (NumberFormatException e) {
            numero1 = 0;
        }

        operacaoPendente = operacao;
        equacaoDisplay.setLength(0);
        equacaoDisplay.append(formatarNumero(numero1)).append(" ").append(operacao).append(" ");
        novaOperacao = true;

        atualizarDisplay();
    }

    // Botão "="
    public void calcularResultado(View view) {
        try {
            if (operacaoPendente == null) {
                if (currentNumber.length() > 0) {
                    String numeroAtual = currentNumber.toString();
                    textViewHistorico.setText(numeroAtual + " =");
                    textViewResultado.setText(numeroAtual);
                }
                equacaoDisplay.setLength(0);
                novaOperacao = true;
                return;
            }

            if (novaOperacao && currentNumber.length() == 0) {
                double resultadoFinal = executarCalculo(numero1, numero1, operacaoPendente);
                String resultadoFormatado = formatarNumero(resultadoFinal);

                historicoCompleto.add(formatarNumero(numero1) + " " + operacaoPendente + " " + formatarNumero(numero1) + " = " + resultadoFormatado);

                textViewHistorico.setText(formatarNumero(numero1) + " " + operacaoPendente + " " + formatarNumero(numero1) + " =");
                textViewResultado.setText(resultadoFormatado);

                numero1 = resultadoFinal;
                currentNumber.setLength(0);
                currentNumber.append(resultadoFormatado);
                operacaoPendente = null;
                novaOperacao = true;
                equacaoDisplay.setLength(0);
                return;
            }

            double numero2 = Double.parseDouble(currentNumber.toString());
            String operacaoFinal = operacaoPendente;

            double resultadoFinal = executarCalculo(numero1, numero2, operacaoFinal);

            String expressaoFinal = equacaoDisplay.toString() + formatarNumero(numero2);
            String resultadoFormatado = formatarNumero(resultadoFinal);

            // Adição da expressão ao histórico completo (mantida na sua lógica)
            historicoCompleto.add(expressaoFinal + " = " + resultadoFormatado);

            textViewHistorico.setText(expressaoFinal + " =");
            textViewResultado.setText(resultadoFormatado);

            numero1 = resultadoFinal;
            currentNumber.setLength(0);
            currentNumber.append(resultadoFormatado);
            operacaoPendente = null;
            novaOperacao = true;
            equacaoDisplay.setLength(0);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Número inválido.", Toast.LENGTH_SHORT).show();
            limpar(null);
        }
    }

    // Operações unárias (+/-, %)
    public void calcularUnario(View view) {
        String tipo = ((android.widget.Button) view).getText().toString();
        try {
            double numero = Double.parseDouble(currentNumber.toString());
            double resultado = numero;

            if (tipo.equals("%")) {
                resultado = numero / 100;
            } else if (tipo.equals("+/-")) {
                resultado = -numero;
            }

            currentNumber.setLength(0);
            currentNumber.append(formatarNumero(resultado));
            atualizarDisplay();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Erro ao aplicar operação.", Toast.LENGTH_SHORT).show();
        }
    }

    // Apagar último dígito
    public void apagarDigito(View view) {
        if (currentNumber.length() > 1) {
            currentNumber.deleteCharAt(currentNumber.length() - 1);
        } else {
            currentNumber.setLength(0);
            currentNumber.append("0");
        }
        atualizarDisplay();
    }

    // Limpar tudo
    public void limpar(View view) {
        currentNumber.setLength(0);
        currentNumber.append("0");
        equacaoDisplay.setLength(0);
        operacaoPendente = null;
        numero1 = 0;
        novaOperacao = false;
        atualizarDisplay();
    }

    // Mostrar histórico completo (AGORA COM ALERTDIALOG)
    public void mostrarHistoricoCompleto(View view) {
        if (historicoCompleto.isEmpty()) {
            Toast.makeText(this, "Histórico vazio.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] itensHistorico = historicoCompleto.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Histórico de Cálculos");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                itensHistorico
        );

        builder.setAdapter(adapter, null);

        builder.setPositiveButton("Fechar", (dialog, id) -> dialog.dismiss());

        builder.setNegativeButton("Limpar Histórico", (dialog, id) -> {
            historicoCompleto.clear();
            Toast.makeText(MainActivity.this, "Histórico limpo.", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    // Função de cálculo principal
    private double executarCalculo(double num1, double num2, String operacao) {
        switch (operacao) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "×":
            case "*":
                return num1 * num2;
            case "÷":
            case "/":
                if (num2 == 0) {
                    Toast.makeText(this, "Divisão por zero!", Toast.LENGTH_SHORT).show();
                    return 0;
                }
                return num1 / num2;
            default:
                return num2;
        }
    }

    // Atualiza o texto da tela
    private void atualizarDisplay() {
        textViewResultado.setText(currentNumber.toString());
        textViewHistorico.setText(equacaoDisplay.toString());
    }

    // Formata números removendo .0 desnecessário (Versão mais robusta)
    private String formatarNumero(double num) {
        if (num == (long) num) {
            return String.format(Locale.US, "%d", (long) num);
        } else {
            // Usa String.format para garantir que o formato de ponto flutuante seja consistente
            return String.format(Locale.US, "%s", num);
        }
    }
}