import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.IndexMinPQ;
import java.awt.Color;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

public class SeamCarver {
  private final List<Integer> reds;
  private final List<Integer> greens;
  private final List<Integer> blues;

  private int height;

  public SeamCarver(Picture picture) {
    if (picture == null)
      throw new IllegalArgumentException("constructor called with null argument.");
    this.height = picture.height();

    reds = new ArrayList<>();
    greens = new ArrayList<>();
    blues = new ArrayList<>();

    for (int i = 0; i < picture.width(); i++)
      for (int j = 0; j < picture.height(); j++) {
        Color color = picture.get(i, j);
        reds.add(color.getRed());
        greens.add(color.getGreen());
        blues.add(color.getBlue());
      }
  }

  public Picture picture() {
    Picture newPicture = new Picture(width(), height());

    for (int i = 0; i < width(); i++)
      for (int j = 0; j < height(); j++) {
        int pixelIdx = i * height() + j;
        newPicture.set(
            i, j, new Color(reds.get(pixelIdx), greens.get(pixelIdx), blues.get(pixelIdx)));
      }
    return newPicture;
  }

  public int width() {
    return reds.size() / height;
  }

  public int height() {
    return height;
  }

  public double energy(int x, int y) {
    if (x < 0 || x > width() - 1 || y < 0 || y > height() - 1) throw new IllegalArgumentException();
    double eng = 1000;

    if (x > 0 && x < width() - 1 && y > 0 && y < height() - 1) {
      int rightIdx = (x + 1) * height() + y;
      int leftIdx = (x - 1) * height() + y;
      int downIdx = x * height() + y + 1;
      int upIdx = x * height() + y - 1;

      double Rxs = Math.pow(reds.get(rightIdx) - reds.get(leftIdx), 2);
      double Bxs = Math.pow(blues.get(rightIdx) - blues.get(leftIdx), 2);
      double Gxs = Math.pow(greens.get(rightIdx) - greens.get(leftIdx), 2);

      double Rys = Math.pow(reds.get(upIdx) - reds.get(downIdx), 2);
      double Bys = Math.pow(blues.get(upIdx) - blues.get(downIdx), 2);
      double Gys = Math.pow(greens.get(upIdx) - greens.get(downIdx), 2);

      eng = Math.sqrt(Rxs + Bxs + Gxs + Rys + Bys + Gys);
    }

    return eng;
  }

  public int[] findHorizontalSeam() {
    int[] prev = new int[width() * height()];
    IndexMinPQ<Double> pq = new IndexMinPQ<>(width() * height());

    for (int i = 0; i < height(); i++) pq.insert(i, 1000.0);

    int lastIdx;
    Set<Integer> processed = new HashSet<>();

    while (true) {
      double minCost = pq.minKey();
      int minIdx = pq.delMin();
      processed.add(minIdx);
      int minx = minIdx / height();
      int miny = minIdx % height();

      if (minx == width() - 1) {
        lastIdx = minIdx;
        break;
      }

      int rightIdx = (minx + 1) * height() + miny;
      int upIdx = (minx + 1) * height() + miny - 1;
      int downIdx = (minx + 1) * height() + miny + 1;

      if (!processed.contains(rightIdx)) {
        if (!pq.contains(rightIdx)) {
          pq.insert(rightIdx, minCost + energy(minx + 1, miny));
          prev[rightIdx] = 0;
        } else {
          double rightCost = pq.keyOf(rightIdx);
          double newCost = minCost + energy(minx + 1, miny);

          if (newCost < rightCost) {
            pq.decreaseKey(rightIdx, newCost);
            prev[rightIdx] = 0;
          }
        }
      }

      if (!processed.contains(upIdx)) {
        if (miny > 0) {
          double newCost = minCost + energy(minx + 1, miny - 1);
          if (!pq.contains(upIdx)) {
            pq.insert(upIdx, newCost);
            prev[upIdx] = 1;
          } else {
            double upCost = pq.keyOf(upIdx);

            if (newCost < upCost) {
              pq.decreaseKey(upIdx, newCost);
              prev[upIdx] = 1;
            }
          }
        }
      }

      if (!processed.contains(downIdx)) {
        if (miny < height() - 1) {
          double newCost = minCost + energy(minx + 1, miny + 1);
          if (!pq.contains(downIdx)) {
            pq.insert(downIdx, newCost);
            prev[downIdx] = -1;
          } else {
            double downCost = pq.keyOf(downIdx);

            if (newCost < downCost) {
              pq.decreaseKey(downIdx, newCost);
              prev[downIdx] = -1;
            }
          }
        }
      }
    }

    int lastX = lastIdx / height();
    int lastY = lastIdx % height();

    if (lastX != width() - 1) throw new UnknownError("Something is wrong here!");

    int[] path = new int[width()];

    for (int curX = lastX, curY = lastY; curX >= 0; curX--) {
      path[curX] = curY;
      curY += prev[curX * height() + curY];
    }

    return path;
  }

  public int[] findVerticalSeam() {
    int[] prev = new int[width() * height()];
    IndexMinPQ<Double> pq = new IndexMinPQ<>(width() * height());

    for (int i = 0; i < width(); i++) pq.insert(i * height(), 1000.0);

    int lastIdx;
    Set<Integer> processed = new HashSet<>();

    while (true) {
      double minCost = pq.minKey();
      int minIdx = pq.delMin();
      processed.add(minIdx);
      int minx = minIdx / height();
      int miny = minIdx % height();

      if (miny == height() - 1) {
        lastIdx = minIdx;
        break;
      }

      int downIdx = minx * height() + miny + 1;
      int leftIdx = (minx - 1) * height() + miny + 1;
      int rightIdx = (minx + 1) * height() + miny + 1;

      if (!processed.contains(downIdx)) {
        if (!pq.contains(downIdx)) {
          pq.insert(downIdx, minCost + energy(minx, miny + 1));
          prev[downIdx] = 0;
        } else {
          double downCost = pq.keyOf(downIdx);
          double newCost = minCost + energy(minx, miny + 1);

          if (newCost < downCost) {
            pq.decreaseKey(downIdx, newCost);
            prev[downIdx] = 0;
          }
        }
      }

      if (!processed.contains(leftIdx)) {
        if (minx > 0) {
          double newCost = minCost + energy(minx - 1, miny + 1);
          if (!pq.contains(leftIdx)) {
            pq.insert(leftIdx, newCost);
            prev[leftIdx] = 1;
          } else {
            double leftCost = pq.keyOf(leftIdx);

            if (newCost < leftCost) {
              pq.decreaseKey(leftIdx, newCost);
              prev[leftIdx] = 1;
            }
          }
        }
      }

      if (!processed.contains(rightIdx)) {
        if (minx < width() - 1) {
          double newCost = minCost + energy(minx + 1, miny + 1);
          if (!pq.contains(rightIdx)) {
            pq.insert(rightIdx, newCost);
            prev[rightIdx] = -1;
          } else {
            double rightCost = pq.keyOf(rightIdx);

            if (newCost < rightCost) {
              pq.decreaseKey(rightIdx, newCost);
              prev[rightIdx] = -1;
            }
          }
        }
      }
    }

    int lastX = lastIdx / height();
    int lastY = lastIdx % height();

    if (lastY != height() - 1) throw new UnknownError("Something is wrong here!");

    int[] path = new int[height()];

    for (int curX = lastX, curY = lastY; curY >= 0; curY--) {
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

    for (int i = width() - 1; i >= 0; i--) {
      reds.remove(i * height() + seam[i]);
      greens.remove(i * height() + seam[i]);
      blues.remove(i * height() + seam[i]);
    }

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

    for (int i = 0; i < height(); i++) {
      for (int j = seam[i]; j < width() - 1; j++) {
        reds.set(j * height() + i, reds.get((j + 1) * height() + i));
        greens.set(j * height() + i, greens.get((j + 1) * height() + i));
        blues.set(j * height() + i, blues.get((j + 1) * height() + i));
      }
    }

    reds.subList((width() - 1) * height(), height() * width()).clear();
  }
}
