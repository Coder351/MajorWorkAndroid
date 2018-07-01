package com.mrinaanksinha.majorworkandroid;

import android.app.ActionBar;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class EquationTools
{

    public final String ERROR_NOT_VALID_EQUATION = "notValidEquationError";

    private static String[] getTextArray(String detectedTextBoxes)
    {
        return detectedTextBoxes.split("\n");
    }

    private static ArrayList<EquationCharacter> getEquationFromBoxes(String detectedTextBoxes)
    {
        if (detectedTextBoxes.isEmpty())
        {
            return null;
        }
        String[] detectedTextArray = getTextArray(detectedTextBoxes);
        ArrayList<EquationCharacter> equationArray = new ArrayList<EquationCharacter>();
        for (String character : detectedTextArray)
        {
            equationArray.add(new EquationCharacter(character));
        }

        return equationArray;
    }

    private static Boolean isOperator(String testChar)
    {
        return "+-/*x^".contains(testChar);
    }


    private static boolean isNum(String element)
    {    // check char is Number (Pi is number too)
        for (Character c : element.toCharArray())
        {
            if (!Character.isDigit(c))
            {
                return false;
            }
        }

        return true;

    }

    public static String standardizeEquationToInfix(String rawCameraText, Size imageBounds)
    {
        ArrayList<EquationCharacter> equationArray = getEquationFromBoxes(rawCameraText);
        if (equationArray == null)
        {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Boolean previousIsExponent = false;
        for (int i = 0; i < equationArray.size(); i++)
        {
            EquationCharacter character = equationArray.get(i);
            String element = character.getCharacter();
            Boolean isExponent = false;
            if (element.equals("~"))
            {
                continue;
            }
            if (element.equals("="))
            {
                break;
            }
            if (character.getRight() >= 2 * imageBounds.getHeight() / 3)
            {
                element = " ^ " + element;
                isExponent = true;
            }

            if (isOperator(character.getCharacter()))
            {
                element = " " + element + " ";
            }
            if ((isOperator(character.getCharacter()) || isExponent) && (isOperator(equationArray.get(Math.max(i - 1, 0)).getCharacter()) || previousIsExponent || i == 0 || i == equationArray.size() - 1))
            {
                return null;
            }

            builder.append(element);
            previousIsExponent = isExponent;
        }

        builder = new StringBuilder(builder.toString().trim());

        int open = 0, close = 0;
        for (int i = 0; i < builder.length(); i++)
        {
            char c = builder.charAt(i);
            if (c == '(')
            {
                open++;
            }
            if (c == ')')
            {
                close++;
            }
        }
        if (open < close)
        {
            builder.insert(0, "( ");
        }
        for (int i = 0; i < (open - close); i++)
        {// auto append ")"
            builder.append(" ) ");
        }
        String rawEquation = builder.toString();
        builder.setLength(0);

        for (int i = 0; i < rawEquation.length(); i++)
        {
            if (i > 0 && isLParenthesis(Character.toString(rawEquation.charAt(i))) && (rawEquation.charAt(i - 1) == ')' || isNum(Character.toString(rawEquation.charAt(i - 1)))))
            {
                builder.append("* "); //	fix ...)(... to ...)*(...
            }
//            if ((i == 0 || (i > 0 && !isNum(Character.toString(rawEquation.charAt(i - 1))))) && rawEquation.charAt(i) == '-' && isNum(Character.toString(rawEquation.charAt(i + 1))))
//            {
//                builder.append("~"); // check so am
//            }
            if (i > 0 && ((rawEquation.charAt(i - 1) == '(' && isNum(Character.toString(rawEquation.charAt(i)))) || (rawEquation.charAt(i) == ')' && isNum(Character.toString(rawEquation.charAt(i - 1))))))
            {
                builder.append(" ");
            }

            builder.append(rawEquation.charAt(i));
        }

        if (!checkValidEquation(builder.toString()))
        {
            return "";
        }

        return builder.toString();
    }

    private static boolean checkValidEquation(String equation)
    {
        if (equation.isEmpty())
        {
            return false;
        }
        if (!(equation.contains("+")
                || equation.contains("-")
                || equation.contains("*")
                || equation.contains("/")
                || equation.contains("x")
                || equation.contains("^")))
        {
            return false;
        }
        if (!(equation.contains("0")
                || equation.contains("1")
                || equation.contains("2")
                || equation.contains("3")
                || equation.contains("4")
                || equation.contains("5")
                || equation.contains("6")
                || equation.contains("7")
                || equation.contains("8")
                || equation.contains("9")))
        {
            return false;
        }
        return true;
    }

    private static Boolean isLParenthesis(String element)
    {
        return element.equals("(");
    }

    private static Boolean isRParenthesis(String element)
    {
        return element.equals(")");
    }

    private static int precedence(String element)
    {        // setting priority
        switch (element)
        {
            case "+":
            case "-":
                return 1;
            case "*":
            case "x":
            case "/":
                return 2;
            case "^":
                return 3;
        }
        return 0;
    }

    public static ArrayList<String> infixToPostfix(String infix)
    {
//        String error = "";
        String[] infixArray = infix.split(" ");
        Stack<String> stack = new Stack<>();
        ArrayList<String> postfixArray = new ArrayList<>();
        for (String character : infixArray)
        {
            if (isNum(character))
            {
                postfixArray.add(character);
            }
            else if (isLParenthesis(character))
            {
                stack.push(character);
            }
            else if (isRParenthesis(character))
            {
                String stackTop = "";
                do
                {
                    stackTop = stack.peek();
                    if (!isLParenthesis(stackTop))
                    {
                        postfixArray.add(stackTop);
                    }
                    stack.pop();

                } while (!isLParenthesis(stackTop));
//                if (!isLParenthesis(stackTop) && stack.empty())
//                {
//                    error = "Missing left Parenthesis";
//                }
            }
            else
            {
                while (!(stack.empty() || isLParenthesis(stack.peek()) || precedence(stack.peek()) < precedence(character) || (precedence(stack.peek()) == precedence(character) && !isLeftAssociative(stack.peek()))))
                {
                    postfixArray.add(stack.peek());
                    stack.pop();
                }
                stack.push(character);
            }

        }

        //            if (isLParenthesis(element))
        //            {
        //                error = "Missing right parenthesis";
        //            }

        postfixArray.addAll(stack);

        return postfixArray;
    }

    private static boolean isLeftAssociative(String element)
    {
        switch (element)
        {
            case "+":
            case "-":
            case "*":
            case "x":
            case "/":
                return true;
            case "^":
                return false;
        }
        return true;
    }

    public static String solvePostfix(ArrayList<String> postfix)
    {
        Stack<Double> stack = new Stack<>();

        Double answer = null;
        try
        {
            for (String element : postfix)
            {
                if (isNum(element))
                {
                    stack.push(Double.parseDouble(element));
                }
                else
                {
                    Double num2 = stack.peek();
                    stack.pop();
                    Double num1 = stack.peek();
                    stack.pop();
                    switch (element)
                    {
                        case "+":
                            stack.push(num1 + num2);
                            break;
                        case "-":
                            stack.push(num1 - num2);
                            break;
                        case "*":
                        case "x":
                            stack.push(num1 * num2);
                            break;
                        case "/":
                            stack.push(num1 / num2);
                            break;
                        case "^":
                            stack.push(Math.pow(num1, num2));
                            break;
                        default:
                            stack.push(num2);
                            stack.push(num1);
                            Log.d("TAGAA postfix operation", "error with operator/unkown element: " + element);

                    }
                }
            }
            answer = stack.peek();
            stack.pop();
            if (!stack.empty())
            {
                Log.d("TAGA postfix operation", "error with multiple answers!");
            }
        }
        catch (EmptyStackException e)
        {
            e.printStackTrace();
        }

        if (answer == Math.rint(answer))
        {
            return Long.toString(Math.round(answer));
        }
        else
        {
            return Double.toString(answer);
        }
    }
}


