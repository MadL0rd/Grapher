package view.grapher.graphics;

import calculator2.calculator.executors.Expression;
import calculator2.calculator.executors.Variable;
import model.GraphType;
import view.grapher.CoordinateSystem;

import java.awt.*;

import static view.MainPanel.GRAPH_WIDTH;
import static view.MainPanel.HEIGHT;
import static view.grapher.CoordinateSystem.mod;
public class Translation extends Graphic {
    private double[][] dataX;
    private double[][] dataY;
    private int endY;
    private final CoordinateSystem cs;
    private Expression<Double> yFunc;
    private Variable<Double> xy;
    private Variable<Double> yx;
    private Variable<Double> yy;
    private int multiplyer = 2;
    public Translation(CoordinateSystem cs){
        this.cs = cs;
        setMAP_SIZE(200);
        super.type = GraphType.TRANSLATION;
    }
    public Translation(CoordinateSystem cs, int map_size, boolean feelsTime){
        this.cs = cs;
        MAP_SIZE = map_size;
        this.feelsTime = feelsTime;
        super.type = GraphType.TRANSLATION;
    }
    @Override
    public synchronized void resize(double offsetX, double offsetY, double scaleX, double scaleY) {
        if(dataX.length/multiplyer < cs.MAX_LINES)
            resetMAP_SIZE(cs.MAX_LINES * multiplyer);
        if (needResize || offsetX != this.offsetX || this.scaleX != scaleX
                || offsetY != this.offsetY || this.scaleY != scaleY) {
            this.offsetY = offsetY;
            this.scaleY = scaleY;
            this.offsetX = offsetX;
            this.scaleX = scaleX;
            needResize = false;
            double deltaX = cs.getDeltaX() / multiplyer;
            double deltaY = cs.getDeltaY() / multiplyer;
            endY = (int) (GRAPH_WIDTH / scaleX / deltaX) + 3;
            int endX = (int) (HEIGHT / scaleY / deltaY) + 3;
            double lineStart = -mod(offsetX, deltaX) + offsetX;
            double lineEnd = lineStart + (endY - 1)*deltaX;
            for (int n = 0; n < endX; ++n) {
                double[] mapX = dataX[n];
                double[] mapY = dataY[n];
                double y = -(mod(offsetY, deltaY) + (endX - n - 2) * deltaY) + offsetY;
                for (int i = 0; i < MAP_SIZE; ++i) {
                    double x = (i*lineStart + (MAP_SIZE - i - 1)*lineEnd)/MAP_SIZE;
                    var.setValue(x);
                    xy.setValue(y);
                    mapX[i] = func.calculate();
                    yx.setValue(x);
                    yy.setValue(y);
                    mapY[i] = yFunc.calculate();
                }
            }
            lineStart = -mod(offsetY, deltaY)+ offsetY + deltaX;
            lineEnd = lineStart - (endX -1)*deltaY;
            for (int n = 0; n < endY; ++n) {
                double[] mapX = dataX[n + endX];
                double[] mapY = dataY[n + endX];
                double x = (-mod(offsetX, deltaX) + n * deltaX) + offsetX;
                for (int i = 0; i < MAP_SIZE; ++i) {
                    double y = (i*lineStart + (MAP_SIZE - i - 1)*lineEnd)/MAP_SIZE;
                    var.setValue(x);
                    xy.setValue(y);
                    mapX[i] = func.calculate();
                    yx.setValue(x);
                    yy.setValue(y);
                    mapY[i] = yFunc.calculate();
                }
            }
            endY = endX + endY;
        }
    }

    public void setMultiplyer(int multiplyer) {
        if(this.multiplyer != multiplyer) {
            this.multiplyer = multiplyer;
            this.needResize = true;
        }
    }

    public int getMultiplyer() {
        return multiplyer;
    }

    @Override
    public synchronized void paint(Graphics g) {
        g.setColor(color);
        for(int n = 0; n < endY; ++n){
            double[] map = dataY[n];
            double[] xMap = dataX[n];
            for (int i = 0; i < MAP_SIZE-1; ++i) {
                if (Double.isNaN(map[i] + map[i + 1] + xMap[i] + xMap[i + 1]))
                    continue;
                int y1 = (int) ((offsetY - map[i]) * scaleY);
                int y2 = (int) ((offsetY - map[i + 1]) * scaleY);
                if (y1 < 0 && y2 < 0 || y1 > HEIGHT && y2 > HEIGHT)
                    continue;
                int x1 = (int) ((-offsetX + xMap[i]) * scaleX);
                int x2 = (int) ((-offsetX + xMap[i + 1]) * scaleX);
                if (x1 < 0 || x2 < 0 || x1 > GRAPH_WIDTH && x2 > GRAPH_WIDTH)
                    continue;
                g.drawLine(x1, y1, x2, y2);
            }
        }
    }

    private void resetMAP_SIZE(int size) {
        dataX = new double[size][dataX[0].length];
        dataY = new double[size][dataY[0].length];
        needResize = true;
    }
    public void updateY(Expression<Double> funcY, Variable<Double> xy, Variable<Double> yx, Variable<Double> yy){
        this.yFunc = funcY;
        this.xy = xy;
        this.yx = yx;
        this.yy = yy;
    }
    @Override
    public void setMAP_SIZE(int map_size) {
        int size = 1;
        if(dataX != null)
            size = dataX[0].length;
        while (size < map_size)
            size *= 2;
        this.MAP_SIZE = map_size;
        if(size == map_size)
            return;
        dataX = new double[cs.MAX_LINES * multiplyer][size];
        dataY = new double[cs.MAX_LINES * multiplyer][size];
    }
}
