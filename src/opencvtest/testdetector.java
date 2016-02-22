package opencvtest;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class testdetector {

	public void closeFrame() {

	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		final JFrame frame = new JFrame("webcam");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 480);
		ImagePanel facePanel = new ImagePanel();
		frame.setContentPane(facePanel);

		JButton closeButton = new JButton("Закрыть");

		facePanel.add(closeButton, BorderLayout.SOUTH);

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		final VideoCapture vid = new VideoCapture();

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vid.release();
				frame.setVisible(false);
				frame.dispose();
				System.exit(0);
			}
		});

		vid.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 640);
		vid.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 480);
		frame.setVisible(true);
		vid.open(0);

		if (!vid.isOpened()) {
			System.out.println("not found webcam");
		} else {
			System.out.println("found: " + vid.toString());
		}

		Mat camImage = new Mat();
		Mat hsvimage = new Mat();
		Mat blur = new Mat();
		Scalar lowerb = new Scalar(160, 100, 100);
		Scalar upperb = new Scalar(179, 255, 255);

		if (vid.isOpened()) {
			while (true) {
				vid.read(camImage);
				Imgproc.cvtColor(camImage, hsvimage, Imgproc.COLOR_BGR2HSV);
				Imgproc.GaussianBlur(hsvimage, blur, new Size(9, 9), 2, 2);
				Core.inRange(blur, lowerb, upperb, blur);
				if (!camImage.empty()) {

					MatToBufImg converter = new MatToBufImg(blur, ".jpg");

					List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
					Imgproc.findContours(blur.clone(), contours, new Mat(),
							Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

					if (contours.size() > 0) {
						MatOfPoint maxCountour = contours.get(0);
						double maxArea = Imgproc.contourArea(maxCountour);
						for (int i = 1; i < contours.size(); i++) {
							double curArea = Imgproc.contourArea(contours
									.get(i));
							if (curArea > maxArea) {
								maxArea = curArea;
								maxCountour = contours.get(i);
							}
						}
						Point center = new Point();
						float[] radius = new float[3];
						Imgproc.minEnclosingCircle(
								new MatOfPoint2f(maxCountour.toArray()),
								center, radius);
						if (radius[0] > 40) {
							System.out.println(Imgproc.contourArea(maxCountour)
									+ "x " + center.x + "y " + center.y + "r "
									+ radius[0]);
						}
					}

					BufferedImage img = converter.getImage();
					facePanel.setImage(img);
					facePanel.repaint();

				}
			}
		}

	}

}
