package com.mrinaanksinha.majorworkandroid;

public class EquationCharacter
{
    private String character;
    private int left;
    private int top;
    private int right;
    private int bottom;

    public EquationCharacter(String boxCharacterText)
    {
        String[] splitBoxCharacterText = boxCharacterText.split(" ");
        character = splitBoxCharacterText[0];
        left = Integer.parseInt(splitBoxCharacterText[1]);
        right = Integer.parseInt(splitBoxCharacterText[2]);
        top = Integer.parseInt(splitBoxCharacterText[3]);
        bottom = Integer.parseInt(splitBoxCharacterText[4]);
    }


    public int getLeft()
    {
        return left;
    }

    public int getTop()
    {
        return top;
    }

    public int getRight()
    {
        return right;
    }

    public int getBottom()
    {
        return bottom;
    }

    public String getCharacter()
    {
        return character;
    }
}
