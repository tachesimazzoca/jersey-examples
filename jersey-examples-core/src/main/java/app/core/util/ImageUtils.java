package app.core.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public class ImageUtils {
    private ImageUtils() {
        throw new UnsupportedOperationException();
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    private static ImageReader createImageReader(ImageInputStream input) throws IOException {
        ImageReader ir;
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        if (readers != null && readers.hasNext()) {
            ir = readers.next();
            ir.setInput(input);
        } else {
            throw new IllegalArgumentException("No available image readers.");
        }
        return ir;
    }

    private static ImageWriter createImageWriter(
            ImageOutputStream output, ImageReader reader) throws IOException {
        ImageWriter iw = ImageIO.getImageWriter(reader);
        if (iw == null)
            throw new IllegalArgumentException("No available image writers.");
        iw.setOutput(output);
        return iw;
    }

    private static ImageWriter createImageWriter(
            ImageOutputStream output, String formatName) throws IOException {
        ImageWriter iw;
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        if (writers != null && writers.hasNext()) {
            iw = writers.next();
            iw.setOutput(output);
        } else {
            throw new IllegalArgumentException("No available image writers.");
        }
        return iw;
    }

    private static IIOImage[] resize(
            IIOImage[] images, Integer boundaryW, Integer boundaryH) {
        // no affected
        if (boundaryW == null && boundaryH == null)
            return images;

        IIOImage[] imgs = new IIOImage[images.length];
        for (int i = 0; i < images.length; i++) {
            BufferedImage bimg = (BufferedImage) images[i].getRenderedImage();

            // maximum scale width & height
            int sourceW = bimg.getWidth();
            int sourceH = bimg.getHeight();
            int gw = (boundaryW == null) ? sourceW : boundaryW;
            int gh = (boundaryH == null) ? (sourceH * gw / sourceW) : boundaryH;
            int w = sourceW;
            int h = sourceH;
            if (w > gw) {
                w = gw;
                h = sourceH * w / sourceW;
            }
            if (h > gh) {
                h = gh;
                w = sourceW * h / sourceH;
            }

            ColorModel cm = bimg.getColorModel();
            boolean transparentGIF = cm.hasAlpha() && (cm instanceof IndexColorModel);
            // convert if the image is not a transparent GIF
            if (!transparentGIF && (w != bimg.getWidth() || h != bimg.getHeight())) {
                BufferedImage buf;
                if (cm instanceof IndexColorModel)
                    buf = new BufferedImage(w, h, bimg.getType(), (IndexColorModel) cm);
                else
                    buf = new BufferedImage(w, h, bimg.getType());
                Graphics2D g2d = buf.createGraphics();
                g2d.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(bimg, 0, 0, w, h, null);
                g2d.dispose();
                imgs[i] = new IIOImage(buf, null, null);
            } else {
                imgs[i] = images[i];
            }
        }
        return imgs;
    }

    public static void convert(
            InputStream input,
            OutputStream output,
            String formatName,
            Integer width, Integer height) throws IOException {

        ImageReader ir = null;
        ImageWriter iw = null;
        ImageOutputStream ios = null;
        try {
            ImageIO.setUseCache(false);

            // reader
            ImageInputStream iis = ImageIO.createImageInputStream(input);
            ir = createImageReader(iis);

            // writer & formatName
            final String inputFormatName = ir.getFormatName();
            ios = ImageIO.createImageOutputStream(output);
            if (formatName != null) {
                iw = createImageWriter(ios, formatName);
            } else {
                iw = createImageWriter(ios, ir);
            }
            // prepare an array of IIOImage
            final int N = ir.getNumImages(true);
            IIOImage[] imgs = new IIOImage[N];
            for (int i = 0; i < N; i++) {
                imgs[i] = ir.readAll(i, null);
                IIOMetadata metadata = imgs[i].getMetadata();
                if (metadata.isReadOnly())
                    metadata = iw.getDefaultImageMetadata(ir.getRawImageType(i), null);
                imgs[i].setMetadata(metadata);
            }

            // strip
            for (int i = 0; i < imgs.length; i++) {
                imgs[i].setThumbnails(null);
                imgs[i].setMetadata(null);
            }

            // resize
            imgs = resize(imgs, width, height);

            // write images
            if (formatName == null || formatName.equals(inputFormatName)) {
                if (imgs.length == 1) {
                    iw.write(imgs[0]);
                } else {
                    iw.prepareWriteSequence(imgs[0].getMetadata());
                    for (int i = 0; i < imgs.length; i++) {
                        iw.writeToSequence(imgs[i], null);
                    }
                    iw.endWriteSequence();
                }
            } else {
                // convert file format
                BufferedImage bimg = (BufferedImage) imgs[0].getRenderedImage();
                if (formatName.equals("png") || !bimg.getColorModel().hasAlpha()) {
                    iw.write(new IIOImage(bimg, null, null));
                } else {
                    BufferedImage buf = new BufferedImage(bimg.getWidth(), bimg.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = buf.createGraphics();
                    g2d.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(bimg, 0, 0, bimg.getWidth(), bimg.getHeight(), null);
                    g2d.dispose();
                    iw.write(new IIOImage(buf, null, null));
                }
            }

        } catch (IOException e) {
            throw e;
        } finally {
            if (ir != null)
                ir.dispose();
            if (iw != null)
                iw.dispose();
            closeQuietly(ios);
        }
    }
}
