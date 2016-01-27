/*
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 */
package org.codice.imaging.nitf.render.flow;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.codice.imaging.nitf.core.NitfFileHeader;
import org.codice.imaging.nitf.core.SlottedNitfParseStrategy;
import org.codice.imaging.nitf.core.dataextension.NitfDataExtensionSegmentHeader;
import org.codice.imaging.nitf.core.image.NitfImageSegmentHeader;

/**
 * The NitfSegmentsFlow provides methods for processing the contents of the NITF file.
 */
public class NitfSegmentsFlow {

    private SlottedNitfParseStrategy parseStrategy;

    NitfSegmentsFlow(SlottedNitfParseStrategy parseStrategy) {
        if (parseStrategy == null) {
            throw new IllegalArgumentException(
                    "ImageSegmentFlow(): constructor argument 'parseStrategy' may not be null.");
        }

        this.parseStrategy = parseStrategy;
    }

    /**
     * Iterates over the images in the NITF file and passes them to the supplied consumer.
     *
     * @param consumer The consumer to pass the image header and image data to.
     * @return this NitfSegmentsFlow.
     */
    public NitfSegmentsFlow forEachImage(BiConsumer<NitfImageSegmentHeader, ImageInputStream> consumer) {

        List<byte[]> imageDataArrays = parseStrategy.getImageSegmentData();
        List<NitfImageSegmentHeader> headers = parseStrategy.getImageSegmentHeaders();

        for (int i = 0; i < imageDataArrays.size(); i++) {
            byte[] imageData = imageDataArrays.get(i);
            NitfImageSegmentHeader header = headers.get(i);
            ImageInputStream imageInputStream = new MemoryCacheImageInputStream(new ByteArrayInputStream(imageData));
            consumer.accept(header, imageInputStream);
        }

        return this;
    }

    /**
     * Iterates over the data extension segments in the NITF file and passes them to the supplied consumer.
     *
     * @param consumer The consumer to pass the data extension segment to.
     * @return this NitfSegmentsFlow.
     */
    public NitfSegmentsFlow forEachDataSegment(Consumer<NitfDataExtensionSegmentHeader> consumer) {
        List<NitfDataExtensionSegmentHeader> headers = parseStrategy.getDataExtensionSegmentHeaders();

        for (NitfDataExtensionSegmentHeader header : headers) {
            consumer.accept(header);
        }

        return this;
    }

    /**
     * Passes the NITF file header to the supplied consumer.
     *
     * @param consumer the consumer to pass the NITF file header to.
     * @return this NitfSegmentsFlow.
     */
    public NitfSegmentsFlow fileHeader(Consumer<NitfFileHeader> consumer) {
        NitfFileHeader nitfFileHeader = parseStrategy.getNitfHeader();
        consumer.accept(nitfFileHeader);
        return this;
    }
}