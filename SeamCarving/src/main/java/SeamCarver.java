import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.IndexMinPQ;
import java.awt.Color;
import java.util.Set;
import java.util.HashSet;

public class SeamCarver {
  private Picture picture;

  public SeamCarver(Picture picture) {
    if (picture == null)
      throw new IllegalArgumentException("constructor called with null argument.");

    this.picture = new Picture(picture);
  }

  public Picture picture() {
    return new Picture(picture);
  }

  public int width() {
    return picture.width();
  }

  public int height() {
    return picture.height();
  }

  public double energy(int x, int y) {
    if (x < 0 || x > width() - 1 || y < 0 || y > height() - 1) throw new IllegalArgumentException();
    double eng = 1000;

    if (x > 0 && x < width() - 1 && y > 0 && y < height() - 1) {
      Color rightColor = picture.get(x + 1, y);
      Color leftColor = picture.get(x - 1, y);
      Color upColor = picture.get(x, y - 1);
      Color downColor = picture.get(x, y + 1);

      double Rxs = Math.pow(rightColor.getRed() - leftColor.getRed(), 2);
      double Bxs = Math.pow(rightColor.getBlue() - leftColor.getBlue(), 2);
      double Gxs = Math.pow(rightColor.getGreen() - leftColor.getGreen(), 2);

      double Rys = Math.pow(upColor.getRed() - downColor.getRed(), 2);
      double Bys = Math.pow(upColor.getBlue() - downColor.getBlue(), 2);
      double Gys = Math.pow(upColor.getGreen() - downColor.getGreen(), 2);

      eng = Math.pow(Rxs + Bxs + Gxs + Rys + Bys + Gys, 0.5);
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

    Picture newPicture = new Picture(width(), height() - 1);

    for (int i = 0; i < width(); i++)
      for (int j = 0, k = 0; j < height(); j++, k++)
        if (seam[i] != j) newPicture.set(i, k, picture.get(i, j));
        else k--;

    picture = newPicture;
  }

  public void removeVerticalSeam(int[] seam) {
    if (width() <= 1 || seam == null || seam.length != height())
      throw new IllegalArgumentException();

    for (int i = 0; i < seam.length; i++)
      if (seam[i] < 0
          || seam[i] > width() - 1
          || (i < seam.length - 1 && Math.abs(seam[i] - seam[i + 1]) > 1))
        throw new IllegalArgumentException();

    Picture newPicture = new Picture(width() - 1, height());

    for (int i = 0; i < height(); i++)
      for (int j = 0, k = 0; j < width(); j++, k++)
        if (seam[i] != j) newPicture.set(k, i, picture.get(j, i));
        else k--;

    picture = newPicture;
  }
}
