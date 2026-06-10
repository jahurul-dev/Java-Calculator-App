package com.jahurulislam.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.text.DecimalFormat;

public class MainActivity extends Activity {
    private String inputExpression = "";
    private String lastExpression = "";
    private boolean isResultDisplayed = false;
    private TextView tvScreenExpression;
    private TextView tvScreenResult;
    private DecimalFormat decimalFormat = new DecimalFormat("#.########");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. First, display the splash screen layout .
        setContentView(R.layout.splash_screen);

        // 2, The main calculator will launch after a 2-second (2000 ms) delay.
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 3. The main calculator layout will be loaded here.
                setContentView(R.layout.layout);

                // 4. All calculator button IDs and views are connected.
                tvScreenExpression = findViewById(R.id.tvScreenExpression);
                tvScreenResult = findViewById(R.id.tvScreenResult);

                int[] numberBtnIds = {
                        R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                        R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
                };

                View.OnClickListener numberClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button button = (Button) v;
                        if (isResultDisplayed) {
                            inputExpression = button.getText().toString();
                            isResultDisplayed = false;
                        } else {
                            inputExpression += button.getText().toString();
                        }
                        updateDisplay();
                    }
                };

                for (int id : numberBtnIds) {
                    findViewById(id).setOnClickListener(numberClickListener);
                }

                findViewById(R.id.btnAdd).setOnClickListener(createOperatorClickListener("+"));
                findViewById(R.id.btnSub).setOnClickListener(createOperatorClickListener("-"));
                findViewById(R.id.btnMul).setOnClickListener(createOperatorClickListener("×"));
                findViewById(R.id.btnDiv).setOnClickListener(createOperatorClickListener("÷"));

                findViewById(R.id.btnDot).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isResultDisplayed) {
                            inputExpression = "0.";
                            isResultDisplayed = false;
                        } else {
                            String[] tokens = inputExpression.split("[+\\-×÷]");
                            String currentToken = tokens.length > 0 ? tokens[tokens.length - 1] : "";
                            if (!currentToken.contains(".")) {
                                if (currentToken.isEmpty()) {
                                    inputExpression += "0.";
                                } else {
                                    inputExpression += ".";
                                }
                            }
                        }
                        updateDisplay();
                    }
                });

                findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        // Percent (%) Button Logic
                        findViewById(R.id.btnPercent).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!inputExpression.isEmpty()) {
                                    char lastChar = inputExpression.charAt(inputExpression.length() - 1);
                                    // The % operator is added only when the last character is not an operator.
                                    if (lastChar != '+' && lastChar != '-' && lastChar != '×' && lastChar != '÷') {
                                        inputExpression += "%";
                                        updateDisplay();
                                    }
                                }
                            }
                        });


                inputExpression = "";
                lastExpression = "";
                isResultDisplayed = false;
                tvScreenExpression.setText("");
                tvScreenResult.setText("0");
            }
        });

                findViewById(R.id.btnDel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isResultDisplayed) {
                            inputExpression = "";
                            isResultDisplayed = false;
                        } else if (inputExpression.length() > 0) {
                            inputExpression = inputExpression.substring(0, inputExpression.length() - 1);
                        }
                        updateDisplay();
                    }
                });

                findViewById(R.id.btnEqual).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (inputExpression.isEmpty()) return;

                        try {
                            String formattedExpr = inputExpression.replace("×", "*").replace("÷", "/").replace("%", "/100");
                            double result = eval(formattedExpr);

                            lastExpression = inputExpression + " =";
                            tvScreenExpression.setText(lastExpression);

                            inputExpression = decimalFormat.format(result);
                            tvScreenResult.setText(inputExpression);
                            isResultDisplayed = true;
                        } catch (Exception e) {
                            tvScreenResult.setText("Error");
                            isResultDisplayed = true;
                        }
                    }
                });
            }
        }, 2000);
    }

    private View.OnClickListener createOperatorClickListener(final String operator) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isResultDisplayed) {
                    isResultDisplayed = false;
                }
                if (!inputExpression.isEmpty()) {
                    char lastChar = inputExpression.charAt(inputExpression.length() - 1);
                    if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '÷') {
                        inputExpression = inputExpression.substring(0, inputExpression.length() - 1) + operator;
                    } else {
                        inputExpression += operator;
                    }
                    updateDisplay();
                }
            }
        };
    }

    private void updateDisplay() {
        tvScreenResult.setText(inputExpression.isEmpty() ? "0" : inputExpression);
    }

    private static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0) throw new ArithmeticException("Division by zero");
                        x /= divisor;
                    }
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}

