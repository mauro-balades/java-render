import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

public class Main {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        JSlider headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // slider to control vertical rotation
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // slider to control roll
        JSlider rollSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(rollSlider, BorderLayout.WEST);

        // slider to control FoV
        JSlider FoVSlider = new JSlider(1, 179, 60);
        pane.add(FoVSlider, BorderLayout.NORTH);

        // panel to display render results
        JPanel renderPanel = new JPanel() {
                public void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.GRAY);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    List<Triangle> tris = new ArrayList<>();
                    //A
                    tris.add(new Triangle(new Vertex(-100, 100, 100, 1),
                                          new Vertex(100, 100, 100, 1),
                                          new Vertex(-100, 100, -100, 1),
                                          Color.WHITE));
                    //B
                    tris.add(new Triangle(new Vertex(100, 100, 100, 1),
                                          new Vertex(100, 100, -100, 1),
                                          new Vertex(-100, 100, -100, 1),
                                          Color.WHITE));
                    //C
                    tris.add(new Triangle(new Vertex(100, -100, 100, 1),
                                          new Vertex(100, 100, -100, 1),
                                          new Vertex(100, 100, 100, 1),
                                          Color.WHITE));
                    //D
                    tris.add(new Triangle(new Vertex(100, -100, 100, 1),
                                          new Vertex(100, -100, -100, 1),
                                          new Vertex(100, 100, -100, 1),
                                          Color.WHITE));
                    //E
                    tris.add(new Triangle(new Vertex(-100, -100, 100, 1),
                                          new Vertex(100, -100, 100, 1),
                                          new Vertex(-100, 100, 100, 1),
                                          Color.WHITE));

                    //F
                    tris.add(new Triangle(new Vertex(100, -100, 100, 1),
                                          new Vertex(100, 100, 100, 1),
                                          new Vertex(-100, 100, 100, 1),
                                          Color.WHITE));
                    //G
                    tris.add(new Triangle(new Vertex(-100, -100, 100, 1),
                                          new Vertex(-100, 100, 100, 1),
                                          new Vertex(-100, -100, -100, 1),
                                          Color.WHITE));
                    //H
                    tris.add(new Triangle(new Vertex(-100, 100, 100, 1),
                                          new Vertex(-100, 100, -100, 1),
                                          new Vertex(-100, -100, -100, 1),
                                          Color.WHITE));
                    //I
                    tris.add(new Triangle(new Vertex(-100, 100, -100, 1),
                                          new Vertex(100, 100, -100, 1),
                                          new Vertex(-100, -100, -100, 1),
                                          Color.WHITE));
                    //J
                    tris.add(new Triangle(new Vertex(-100, -100, -100, 1),
                                          new Vertex(100, 100, -100, 1),
                                          new Vertex(100, -100, -100, 1),
                                          Color.WHITE));
                    //K
                    tris.add(new Triangle(new Vertex(100, -100, 100, 1),
                                          new Vertex(-100, -100, 100, 1),
                                          new Vertex(-100, -100, -100, 1),
                                          Color.WHITE));
                    //L
                    tris.add(new Triangle(new Vertex(-100, -100, -100, 1),
                                          new Vertex(100, -100, -100, 1),
                                          new Vertex(100, -100, 100, 1),
                                          Color.WHITE));

                    double heading = Math.toRadians(headingSlider.getValue());
                    Matrix4 headingTransform = new Matrix4(new double[] {
                            Math.cos(heading), 0, -Math.sin(heading), 0,
                            0, 1, 0, 0,
                            Math.sin(heading), 0, Math.cos(heading), 0,
                            0, 0, 0, 1
                        });
                    double pitch = Math.toRadians(pitchSlider.getValue());
                    Matrix4 pitchTransform = new Matrix4(new double[] {
                            1, 0, 0, 0,
                            0, Math.cos(pitch), Math.sin(pitch), 0,
                            0, -Math.sin(pitch), Math.cos(pitch), 0,
                            0, 0, 0, 1
                        });
                    double roll = Math.toRadians(rollSlider.getValue());
                    Matrix4 rollTransform = new Matrix4(new double[] {
                            Math.cos(roll), -Math.sin(roll), 0, 0,
                            Math.sin(roll), Math.cos(roll), 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1
                        });

                    Matrix4 panOutTransform = new Matrix4(new double[] {
                            1, 0, 0, 0,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, -400, 1
                        });

                    double viewportWidth = getWidth();
                    double viewportHeight = getHeight();
                    double fovAngle = Math.toRadians(FoVSlider.getValue());
                    double fov = Math.tan(fovAngle / 2) * 170;

                    Matrix4 transform =
                        headingTransform
                        .multiply(pitchTransform)
                        .multiply(rollTransform)
                        .multiply(panOutTransform);

                    BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                    double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                    // initialize array with extremely far away depths
                    for (int q = 0; q < zBuffer.length; q++) {
                        zBuffer[q] = Double.NEGATIVE_INFINITY;
                    }

                    for (Triangle t : tris) {
                        Vertex v1 = transform.transform(t.v1);
                        Vertex v2 = transform.transform(t.v2);
                        Vertex v3 = transform.transform(t.v3);

                        Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z, v2.w - v1.w);
                        Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z, v3.w - v1.w);
                        Vertex norm = new Vertex(
                                                 ab.y * ac.z - ab.z * ac.y,
                                                 ab.z * ac.x - ab.x * ac.z,
                                                 ab.x * ac.y - ab.y * ac.x,
                                                 1
                                                 );
                        double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                        norm.x /= normalLength;
                        norm.y /= normalLength;
                        norm.z /= normalLength;

                        double angleCos = Math.abs(norm.z);

                        v1.x = v1.x / (-v1.z) * fov;
                        v1.y = v1.y / (-v1.z) * fov;
                        v2.x = v2.x / (-v2.z) * fov;
                        v2.y = v2.y / (-v2.z) * fov;
                        v3.x = v3.x / (-v3.z) * fov;
                        v3.y = v3.y / (-v3.z) * fov;

                        v1.x += viewportWidth / 2;
                        v1.y += viewportHeight / 2;
                        v2.x += viewportWidth / 2;
                        v2.y += viewportHeight / 2;
                        v3.x += viewportWidth / 2;
                        v3.y += viewportHeight / 2;

                        int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                        int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                        int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                        int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                        double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

                        for (int y = minY; y <= maxY; y++) {
                            for (int x = minX; x <= maxX; x++) {
                                double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                                double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                                double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                    double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                                    int zIndex = y * img.getWidth() + x;
                                    if (zBuffer[zIndex] < depth) {
                                        img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
                                        zBuffer[zIndex] = depth;
                                    }
                                }
                            }
                        }

                    }

                    g2.drawImage(img, 0, 0, null);
                }
            };
        pane.add(renderPanel, BorderLayout.CENTER);

        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
        rollSlider.addChangeListener(e -> renderPanel.repaint());
        FoVSlider.addChangeListener(e -> renderPanel.repaint());

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        return new Color(red, green, blue);
    }
}

class Vertex {
    double x;
    double y;
    double z;
    double w;
    Vertex(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
}

class Triangle {
    Vertex v1;
    Vertex v2;
    Vertex v3;
    Color color;
    Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }
}

class Matrix4 {
    double[] values;
    Matrix4(double[] values) {
        this.values = values;
    }
    Matrix4 multiply(Matrix4 other) {
        double[] result = new double[16];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                for (int i = 0; i < 4; i++) {
                    result[row * 4 + col] +=
                        this.values[row * 4 + i] * other.values[i * 4 + col];
                }
            }
        }
        return new Matrix4(result);
    }
    Vertex transform(Vertex in) {
        return new Vertex(
                          in.x * values[0] + in.y * values[4] + in.z * values[8] + in.w * values[12],
                          in.x * values[1] + in.y * values[5] + in.z * values[9] + in.w * values[13],
                          in.x * values[2] + in.y * values[6] + in.z * values[10] + in.w * values[14],
                          in.x * values[3] + in.y * values[7] + in.z * values[11] + in.w * values[15]
                          );
    }
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                sb.append(values[row * 4 + col]);
                if (col != 3) {
                    sb.append(",");
                }
            }
            if (row != 3) {
                sb.append(";\n ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}