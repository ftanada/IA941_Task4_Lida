/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modules;

/**
 *
 * @author ftanada
 */
/**
 * Philippe Legault - 6376254
 *
 * CSI 4106 - Artificial Intelligence I
 * University of Ottawa
 * February 2015
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements a display for a maze (or world)
 */
public class Maze extends JPanel implements MouseListener, MouseMotionListener {

    public static final char SMILEY = 'S',
            OBSTACLE = 'X',
            HOME = 'H',
            EMPTY = ' ';

    private static final Color OBSTACLE_COLOR = new Color(44, 62, 80),
            SMILEY_COLOR = new Color(241, 196, 15),
            SMILEY_HOVER_COLOR = new Color(255, 230, 31),
            EMPTY_COLOR = new Color(236, 240, 241),
            EMPTY_HOVER_COLOR = new Color(214, 218, 219),
            HOME_COLOR = new Color(52, 152, 219);

    private char[][] map;
    private int mapWidth, mapHeight;
    private final double padding = 0.1; // In percent
    private Point origin;
    private Point mousePosition;
    private Point homePosition;
    private double zoomFactor;

    /**
     * A Maze is based on a map, typically generated with the WorldGenerator class.
     */
    public Maze(char[][] map)
    {
        setMap(map);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Initializes the display
     */
    public void setMap(char[][] map){
        this.map = map;
        mapHeight = map.length;
        int width = 0;
        for(int i = 0; i < map.length; i++){
            if(map[i].length > width)
                width = map[i].length;
            for(int j = 0; j < map[i].length; j++)
                if(map[i][j] == HOME)
                    homePosition = new Point(i, j);
        }
        mapWidth = width;
        repaint();
    }

    /**
     * Returns the map backing the display.
     */
    public char[][] getMap(){
        return map;
    }

    /**
     * Returns the position of the "home" cell in the map. We assume that there is only one.
     */
    public Point getHomePosition(){
        return homePosition;
    }

    /**
     * Returns the set of Points where smileys are set.
     */
    public Set<Point> getSmileys(){
        Set<Point> smileys = new HashSet<Point>();
        for(int i = 0; i < map.length; i++){
            for(int j = 0; j < map[i].length; j++)
                if(map[i][j] == SMILEY)
                    smileys.add(new Point(i, j));
        }
        return smileys;
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;

        Dimension panelDimensions = getSize();
        double panelWidth = panelDimensions.getWidth();
        double panelHeight = panelDimensions.getHeight();

        if(mapWidth / panelWidth > mapHeight / panelHeight)
            zoomFactor = panelWidth * (1 - 2 * padding) / mapWidth;
        else
            zoomFactor = panelHeight * (1 - 2 * padding) / mapHeight;

        origin = new Point((int) ((panelWidth - mapWidth * zoomFactor) / 2),
                (int) ((panelHeight - mapHeight * zoomFactor) / 2));

        //Draw the background
        g2.setColor(Color.WHITE);
        g2.clearRect(0, 0, (int) panelWidth, (int) panelHeight);

        //Set font settings
        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Font bigFont = new Font(g2.getFont().getFontName(), Font.PLAIN, 25);
        Font smallFont = new Font(g2.getFont().getFontName(), Font.PLAIN, 15);

        double cellDimen = zoomFactor * mapWidth / map.length;

        //Draw the numbers
        g2.setFont(smallFont);
        g2.setColor(Color.BLACK);
        FontMetrics fm = g2.getFontMetrics();
        for(int i = 0; i < map.length; i++){
            String text = i + "";
            int x = ((int) cellDimen - fm.stringWidth(text)) / 2;
            int y = (fm.getAscent() + ((int) cellDimen - (fm.getAscent() + fm.getDescent())) / 2);
            g2.drawString(text, (int) (origin.getX() + x - cellDimen), (int) (origin.getY() + i * cellDimen + y));
        }
        for(int i = 0; i < map[0].length; i++){
            String text = i + "";
            int x = ((int) cellDimen - fm.stringWidth(text)) / 2;
            int y = (fm.getAscent() + ((int) cellDimen - (fm.getAscent() + fm.getDescent())) / 2);
            g2.drawString(text, (int) (origin.getX() + i * cellDimen + x), (int) (origin.getY() + y - cellDimen));
        }

        //Draw the board
        boolean hovering = false;
        for(int i = 0; i < map.length; i++){
            for(int j = 0; j < map[i].length; j++){
                char type = map[i][j];
                switch(type){
                    case SMILEY: g2.setColor(SMILEY_COLOR);
                        break;
                    case HOME: g2.setColor(HOME_COLOR);
                        break;
                    case OBSTACLE: g2.setColor(OBSTACLE_COLOR);
                        break;
                    case EMPTY:
                    default:g2.setColor(EMPTY_COLOR);
                        break;
                }

                int anchorX = (int) (origin.getX() + cellDimen * j);
                int anchorY = (int) (origin.getY() + cellDimen * i);

                //Check if we're hovering an empty cell
                if((type == EMPTY || type == SMILEY) && mousePosition != null &&
                        mousePosition.getX() == i && mousePosition.getY() == j){

                    g2.setColor(type == SMILEY? SMILEY_HOVER_COLOR: EMPTY_HOVER_COLOR);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    hovering = true;
                }

                g2.fillRect(1 + anchorX, 1 + anchorY, (int) cellDimen - 2, (int) cellDimen - 2);

                String text = null;
                if(type == SMILEY)
                    text = "?";
                else if(type == HOME)
                    text = "?";

                g2.setColor(Color.white);
                if(mousePosition != null && mousePosition.getX() == i && mousePosition.getY() == j && type != SMILEY){
                    text = String.format("(%d, %d)", i, j);
                    if(type == EMPTY)
                        g2.setColor(Color.BLACK);
                    g2.setFont(smallFont);
                    fm = g2.getFontMetrics();
                    int x = ((int) cellDimen - fm.stringWidth(text)) / 2;
                    int y = (fm.getAscent() + ((int) cellDimen - (fm.getAscent() + fm.getDescent())) / 2);
                    g2.drawString(text, anchorX + x, anchorY + y);
                } else if(text != null) {
                    g2.setFont(bigFont);
                    fm = g2.getFontMetrics();
                    int x = ((int) cellDimen - fm.stringWidth(text)) / 2;
                    int y = (fm.getAscent() + ((int) cellDimen - (fm.getAscent() + fm.getDescent())) / 2);
                    g2.drawString(text, anchorX + x, anchorY + y);
                }
            }
        }

        if(!hovering)
            setCursor(Cursor.getDefaultCursor());

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mousePosition = getMapCell(e.getX(), e.getY());
        if(mousePosition != null) {
            int i = (int) mousePosition.getX();
            int j = (int) mousePosition.getY();
            if(map[i][j] == SMILEY)
                map[i][j] = EMPTY;
            else if(map[i][j] == EMPTY)
                map[i][j] = SMILEY;
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition = getMapCell(e.getX(), e.getY());
        repaint();
    }

    private Point getMapCell(double x, double y){
        int i = (int) ((y - origin.getY()) / zoomFactor);
        int j = (int) ((x - origin.getX()) / zoomFactor);
        if(i < 0 || i >= mapWidth)
            return null;
        else if(j < 0 || j >= map[i].length)
            return null;
        return new Point(i, j);
    }
}