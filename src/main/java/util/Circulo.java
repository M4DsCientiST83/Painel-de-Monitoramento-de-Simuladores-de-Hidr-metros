package src.main.java.util;

import org.opencv.core.Point;

public class Circulo 
{
    public Point centro;
    public int raio;
    
    public Circulo(Point centro, int raio) 
    {
        this.centro = centro;
        this.raio = raio;
    }
}