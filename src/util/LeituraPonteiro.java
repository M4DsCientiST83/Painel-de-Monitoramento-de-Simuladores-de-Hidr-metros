package util;

import org.opencv.core.Point;

public class LeituraPonteiro 
{
    private int valor;
    private double angulo;
    private Point centro;
    
    public LeituraPonteiro(int valor, double angulo, Point centro) 
    {
        this.valor = valor;
        this.angulo = angulo;
        this.centro = centro;
    }
    
    public int getValor() { return valor; }
    public double getAngulo() { return angulo; }
    
    @Override
    public String toString() 
    {
        return String.format("Valor=%d (Ângulo=%.1f°)", valor, angulo);
    }
}