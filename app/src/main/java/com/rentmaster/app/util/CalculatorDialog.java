package com.rentmaster.app.util;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.rentmaster.app.R;
import java.util.Locale;

public class CalculatorDialog extends DialogFragment {

    public interface CalculatorListener {
        void onResult(String result);
    }

    private CalculatorListener listener;
    private TextView tvDisplay;
    private StringBuilder currentInput = new StringBuilder();
    private double firstOperand = Double.NaN;
    private String operator = null;

    public void setListener(CalculatorListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_calculator, container, false);

        tvDisplay = view.findViewById(R.id.tv_display);

        int[] numberButtons = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };

        for (int id : numberButtons) {
            view.findViewById(id).setOnClickListener(this::onNumberClick);
        }

        view.findViewById(R.id.btn_dot).setOnClickListener(v -> {
            if (!currentInput.toString().contains(".")) {
                if (currentInput.length() == 0) currentInput.append("0");
                currentInput.append(".");
                updateDisplay();
            }
        });

        view.findViewById(R.id.btn_plus).setOnClickListener(v -> onOperatorClick("+"));
        view.findViewById(R.id.btn_minus).setOnClickListener(v -> onOperatorClick("-"));
        view.findViewById(R.id.btn_multiply).setOnClickListener(v -> onOperatorClick("*"));
        view.findViewById(R.id.btn_divide).setOnClickListener(v -> onOperatorClick("/"));

        view.findViewById(R.id.btn_equals).setOnClickListener(v -> calculate());

        view.findViewById(R.id.btn_clear).setOnClickListener(v -> {
            currentInput.setLength(0);
            firstOperand = Double.NaN;
            operator = null;
            updateDisplay();
        });

        view.findViewById(R.id.btn_backspace).setOnClickListener(v -> {
            if (currentInput.length() > 0) {
                currentInput.setLength(currentInput.length() - 1);
                updateDisplay();
            }
        });

        view.findViewById(R.id.btn_add_to_field).setOnClickListener(v -> {
            if (listener != null) {
                String result = tvDisplay.getText().toString();
                if (!result.equals("Error")) {
                    listener.onResult(result);
                    dismiss();
                }
            }
        });

        return view;
    }

    private void onNumberClick(View v) {
        String number = ((android.widget.Button) v).getText().toString();
        currentInput.append(number);
        updateDisplay();
    }

    private void onOperatorClick(String op) {
        if (!Double.isNaN(firstOperand) && currentInput.length() > 0) {
            calculate();
        }
        
        if (currentInput.length() > 0) {
            firstOperand = Double.parseDouble(currentInput.toString());
            currentInput.setLength(0);
        } else if (Double.isNaN(firstOperand)) {
            firstOperand = 0;
        }
        
        operator = op;
    }

    private void calculate() {
        if (Double.isNaN(firstOperand) || operator == null || currentInput.length() == 0) return;

        double secondOperand = Double.parseDouble(currentInput.toString());
        double result = 0;

        switch (operator) {
            case "+": result = firstOperand + secondOperand; break;
            case "-": result = firstOperand - secondOperand; break;
            case "*": result = firstOperand * secondOperand; break;
            case "/":
                if (secondOperand == 0) {
                    tvDisplay.setText("Error");
                    currentInput.setLength(0);
                    firstOperand = Double.NaN;
                    operator = null;
                    return;
                }
                result = firstOperand / secondOperand;
                break;
        }

        // Format result: Remove trailing zeros if unnecessary
        String resultStr;
        if (result == (long) result) {
            resultStr = String.format(Locale.getDefault(), "%d", (long) result);
        } else {
            resultStr = String.format(Locale.getDefault(), "%.2f", result);
        }

        tvDisplay.setText(resultStr);
        firstOperand = result;
        currentInput.setLength(0);
        operator = null;
    }

    private void updateDisplay() {
        if (currentInput.length() == 0) {
            tvDisplay.setText("0");
        } else {
            tvDisplay.setText(currentInput.toString());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
