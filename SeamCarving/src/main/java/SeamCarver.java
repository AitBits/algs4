import edu.princeton.cs.algs4.Picture;
import java.awt.Color;

public class SeamCarver {
  private final int[] colors;
  private int height;
  private int width;
  private int hremoved;

  public SeamCarver(Picture picture) {
    if (picture == null)
      throw new IllegalArgumentException("constructor called with null argument.");

    hremoved = 0;
    height = picture.height();
    width = picture.width();
    colors = new int[picture.width() * picture.height()];

    for (int i = 0; i < colors.length; i++)
      colors[i] = picture.get(i / height(), i % height()).getRGB();
  }

  public Picture picture() {
    Picture newPicture = new Picture(width(), height());

    for (int i = 0; i < width(); i++)
      for (int j = 0; j < height(); j++)
        newPicture.set(i, j, new Color(colors[i * (height + hremoved) + j]));
    return newPicture;
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  public double energy(int x, int y) {
    if (x < 0 || x > width() - 1 || y < 0 || y > height() - 1) throw new IllegalArgumentException();
    double eng = 1000;

    if (x > 0 && x < width() - 1 && y > 0 && y < height() - 1) {
      int rightColor = colors[(x + 1) * (height + hremoved) + y];
      int leftColor = colors[(x - 1) * (height + hremoved) + y];
      int upColor = colors[x * (height + hremoved) + y - 1];
      int downColor = colors[x * (height + hremoved) + y + 1];

      double rxs = Math.pow((rightColor >>> 16) - (leftColor >>> 16), 2);
      double bxs = Math.pow((rightColor & 0xff) - (leftColor & 0xff), 2);
      double gxs = Math.pow(((rightColor & 0xff00) >>> 8) - ((leftColor & 0xff00) >>> 8), 2);

      double rys = Math.pow((upColor >>> 16) - (downColor >>> 16), 2);
      double bys = Math.pow((upColor & 0xff) - (downColor & 0xff), 2);
      double gys = Math.pow(((upColor & 0xff00) >>> 8) - ((downColor & 0xff00) >>> 8), 2);

      eng = Math.sqrt(rxs + bxs + gxs + rys + bys + gys);
    }

    return eng;
  }

  public int[] findHorizontalSeam() {
    if (height == 1) {
      int[] ans = new int[width];
      for (int i = 0; i < ans.length; i++) ans[i] = 0;
      return ans;
    }
    if (width == 1) return new int[] {height / 2};

    int[] prev = new int[width * height];
    double[] costs = new double[width * height];

    int minX = width - 1;
    int minY = 0;
    double minCost = -1;

    for (int nextIdx = 0; nextIdx < height * width; nextIdx++) {
      int nextx = nextIdx / height;
      int nexty = nextIdx % height;

      double nextCost;

      if (nextx == 0) nextCost = 1000;
      else nextCost = costs[nextIdx];

      if (nextx == width - 1) {
        if (nexty == 0) minCost = nextCost;
        else if (nextCost < minCost) {
          minCost = nextCost;
          minX = nextx;
          minY = nexty;
        }

        continue;
      }

      int rightIdx = (nextx + 1) * height() + nexty;
      int upIdx = (nextx + 1) * height() + nexty - 1;
      int downIdx = (nextx + 1) * height() + nexty + 1;

      double rightCost = costs[rightIdx];
      double newRightCost = nextCost + energy(nextx + 1, nexty);

      if (rightCost == 0 || newRightCost < rightCost) {
        prev[rightIdx] = 0;
        costs[rightIdx] = newRightCost;
      }

      if (nexty > 0) {
        double upCost = costs[upIdx];
        double newCost = nextCost + energy(nextx + 1, nexty - 1);

        if (upCost == 0 || newCost < upCost) {
          prev[upIdx] = 1;
          costs[upIdx] = newCost;
        }
      }

      if (nexty < height - 1) {
        double downCost = costs[downIdx];
        double newCost = nextCost + energy(nextx + 1, nexty + 1);

        if (downCost == 0 || newCost < downCost) {
          prev[downIdx] = -1;
          costs[downIdx] = newCost;
        }
      }
    }

    int[] path = new int[width()];

    for (int curX = minX, curY = minY; curX >= 0; curX--) {
      path[curX] = curY;
      curY += prev[curX * height() + curY];
    }

    return path;
  }

  public int[] findVerticalSeam() {
    if (width == 1) {
      int[] ans = new int[height];
      for (int i = 0; i < ans.length; i++) ans[i] = 0;
      return ans;
    }
    if (height == 1) return new int[] {width / 2};

    int[] prev = new int[width * height];
    double[] costs = new double[width * height];

    int minX = 0;
    int minY = height - 1;
    double minCost = -1;

    for (int nextIdx = 0; nextIdx < height * width;) {
      int nextx = nextIdx / height;
      int nexty = nextIdx % height;

      double nextCost;

      if (nexty == 0) nextCost = 1000;
      else nextCost = costs[nextIdx];

      if (nexty == height - 1) {
        if (nextx == 0) minCost = nextCost;
        else if (nextCost < minCost) {
          minCost = nextCost;
          minX = nextx;
          minY = nexty;
        }
        nextIdx += height;
        continue;
      }

      int downIdx = nextx * height() + nexty + 1;
      int leftIdx = (nextx - 1) * height() + nexty + 1;
      int rightIdx = (nextx + 1) * height() + nexty + 1;

      double downCost = costs[downIdx];
      double newDownCost = nextCost + energy(nextx, nexty + 1);

      if (downCost == 0 || newDownCost < downCost) {
        prev[downIdx] = 0;
        costs[downIdx] = newDownCost;
      }

      if (nextx > 0) {
        double leftCost = costs[leftIdx];
        double newCost = nextCost + energy(nextx - 1, nexty + 1);

        if (leftCost == 0 || newCost < leftCost) {
          prev[leftIdx] = 1;
          costs[leftIdx] = newCost;
        }
      }

      if (nextx < width - 1) {
        double rightCost = costs[rightIdx];
        double newCost = nextCost + energy(nextx + 1, nexty + 1);

        if (rightCost == 0 || newCost < rightCost) {
          prev[rightIdx] = -1;
          costs[rightIdx] = newCost;
        }
      }
      nextIdx = (nextIdx + height) % (height * width - 1);
    }

    int[] path = new int[height];

    for (int curX = minX, curY = minY; curY >= 0; curY--) {
      path[curY] = curX;
      curX += prev[curX * height() + curY];
    }

    return path;
  }

  public void removeHorizontalSeam(int[] seam) {
    if (height() <= 1 || seam == null || seam.length != width())
      throw new IllegalArgumentException();

    for (int i = 0; i < seam.length; i++)
      if (seam[i] < 0
          || seam[i] > height() - 1
          || (i < seam.length - 1 && Math.abs(seam[i] - seam[i + 1]) > 1))
        throw new IllegalArgumentException();

    for (int i = 0; i < width; i++) {
      for (int j = seam[i]; j < height - 1; j++) {
        colors[i * (height + hremoved) + j] = colors[i * (height + hremoved) + j + 1];
      }
    }

    hremoved++;
    height--;
  }

  public void removeVerticalSeam(int[] seam) {
    if (width() <= 1 || seam == null || seam.length != height())
      throw new IllegalArgumentException();

    for (int i = 0; i < seam.length; i++)
      if (seam[i] < 0
          || seam[i] > width() - 1
          || (i < seam.length - 1 && Math.abs(seam[i] - seam[i + 1]) > 1))
        throw new IllegalArgumentException();

    for (int i = 0; i < height; i++) {
      for (int j = seam[i]; j < width - 1; j++) {
        colors[j * (height + hremoved) + i] = colors[(j + 1) * (height + hremoved) + i];
      }
    }

    width--;
  }
}
