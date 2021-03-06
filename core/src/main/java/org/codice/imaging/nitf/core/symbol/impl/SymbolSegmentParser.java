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
package org.codice.imaging.nitf.core.symbol.impl;

import org.codice.imaging.nitf.core.common.impl.AbstractSegmentParser;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.common.ParseStrategy;
import org.codice.imaging.nitf.core.common.NitfReader;
import static org.codice.imaging.nitf.core.graphic.impl.GraphicSegmentConstants.SALVL_LENGTH;
import static org.codice.imaging.nitf.core.graphic.impl.GraphicSegmentConstants.SCOLOR_LENGTH;
import static org.codice.imaging.nitf.core.graphic.impl.GraphicSegmentConstants.SDLVL_LENGTH;
import static org.codice.imaging.nitf.core.graphic.impl.GraphicSegmentConstants.SID_LENGTH;
import static org.codice.imaging.nitf.core.graphic.impl.GraphicSegmentConstants.SLOC_HALF_LENGTH;
import static org.codice.imaging.nitf.core.graphic.impl.GraphicSegmentConstants.SNAME_LENGTH;
import static org.codice.imaging.nitf.core.graphic.impl.GraphicSegmentConstants.SXSHDL_LENGTH;
import static org.codice.imaging.nitf.core.graphic.impl.GraphicSegmentConstants.SXSOFL_LENGTH;
import static org.codice.imaging.nitf.core.graphic.impl.GraphicSegmentConstants.SY;
import org.codice.imaging.nitf.core.security.impl.SecurityMetadataParser;
import static org.codice.imaging.nitf.core.symbol.impl.SymbolConstants.NLIPS_LENGTH;
import static org.codice.imaging.nitf.core.symbol.impl.SymbolConstants.NPIXPL_LENGTH;
import static org.codice.imaging.nitf.core.symbol.impl.SymbolConstants.NWDTH_LENGTH;
import static org.codice.imaging.nitf.core.symbol.impl.SymbolConstants.SNUM_LENGTH;
import static org.codice.imaging.nitf.core.symbol.impl.SymbolConstants.SROT_LENGTH;
import static org.codice.imaging.nitf.core.symbol.impl.SymbolConstants.SYNBPP_LENGTH;
import static org.codice.imaging.nitf.core.symbol.impl.SymbolConstants.SYNELUT_LENGTH;
import static org.codice.imaging.nitf.core.symbol.impl.SymbolConstants.SYTYPE_LENGTH;

import org.codice.imaging.nitf.core.symbol.SymbolColour;
import org.codice.imaging.nitf.core.symbol.SymbolSegment;
import org.codice.imaging.nitf.core.symbol.SymbolType;
import org.codice.imaging.nitf.core.tre.TreCollection;
import org.codice.imaging.nitf.core.tre.TreSource;

/**
    Parser for a symbol segment in a NITF 2.0 file.
*/
public class SymbolSegmentParser extends AbstractSegmentParser {

    private int numberOfEntriesInLUT = 0;
    private int symbolExtendedSubheaderLength = 0;
    static final int NUM_BYTES_IN_GRAYSCALE_LUT_ENTRY = 1;
    static final int NUM_BYTES_IN_COLOUR_LUT_ENTRY = 3;

    private SymbolSegmentImpl segment = null;

    /**
     * Parse SymbolSegment from the specified reader, using the specified parseStrategy.
     *
     * The reader provides the data. The parse strategy selects which data to store.
     *
     * @param nitfReader The NitfReader to read the SymbolSegment from.
     * @param parseStrategy the parsing strategy to use to process the data.
     * @param dataLength the length of the segment data part in bytes, excluding the header
     * @return the parsed SymbolSegment.
     * @throws NitfFormatException when the input from the NitfReader isn't what was expected.
     */
    public final SymbolSegment parse(final NitfReader nitfReader, final ParseStrategy parseStrategy,
            final long dataLength) throws NitfFormatException {
        reader = nitfReader;
        segment = new SymbolSegmentImpl();
        segment.setDataLength(dataLength);
        parsingStrategy = parseStrategy;
        segment.setFileType(nitfReader.getFileType());

        readSY();
        readSID();
        readSNAME();
        segment.setSecurityMetadata(new SecurityMetadataParser().parseSecurityMetadata(reader));
        readENCRYP();
        readSTYPE();
        readNLIPS();
        readNPIXPL();
        readNWDTH();
        readNBPP();
        readSDLVL();
        readSALVL();
        readSLOC();
        readSLOC2();
        readSCOLOR();
        readSNUM();
        readSROT();
        readNELUT();
        for (int i = 0; i < numberOfEntriesInLUT; ++i) {
            readLUTEntry();
        }
        readSXSHDL();
        if (symbolExtendedSubheaderLength > 0) {
            readSXSOFL();
            readSXSHD();
        }
        return segment;
    }

    private void readSY() throws NitfFormatException {
        reader.verifyHeaderMagic(SY);
    }

    private void readSID() throws NitfFormatException {
        segment.setIdentifier(reader.readTrimmedBytes(SID_LENGTH));
    }

    private void readSNAME() throws NitfFormatException {
        segment.setSymbolName(reader.readTrimmedBytes(SNAME_LENGTH));
    }

    private void readSTYPE() throws NitfFormatException {
        String stype = reader.readTrimmedBytes(SYTYPE_LENGTH);
        segment.setSymbolType(SymbolType.getEnumValue(stype));
    }

    private void readNLIPS() throws NitfFormatException {
        segment.setNumberOfLinesPerSymbol(reader.readBytesAsInteger(NLIPS_LENGTH));
    }

    private void readNPIXPL() throws NitfFormatException {
        segment.setNumberOfPixelsPerLine(reader.readBytesAsInteger(NPIXPL_LENGTH));
    }

    private void readNWDTH() throws NitfFormatException {
        segment.setLineWidth(reader.readBytesAsInteger(NWDTH_LENGTH));
    }

    private void readNBPP() throws NitfFormatException {
        segment.setNumberOfBitsPerPixel(reader.readBytesAsInteger(SYNBPP_LENGTH));
    }

    private void readSDLVL() throws NitfFormatException {
        segment.setSymbolDisplayLevel(reader.readBytesAsInteger(SDLVL_LENGTH));
    }

    private void readSALVL() throws NitfFormatException {
        segment.setAttachmentLevel(reader.readBytesAsInteger(SALVL_LENGTH));
    }

    private void readSLOC() throws NitfFormatException {
        segment.setSymbolLocationRow(reader.readBytesAsInteger(SLOC_HALF_LENGTH));
        segment.setSymbolLocationColumn(reader.readBytesAsInteger(SLOC_HALF_LENGTH));
    }

    private void readSLOC2() throws NitfFormatException {
        segment.setSymbolLocation2Row(reader.readBytesAsInteger(SLOC_HALF_LENGTH));
        segment.setSymbolLocation2Column(reader.readBytesAsInteger(SLOC_HALF_LENGTH));
    }

    private void readSCOLOR() throws NitfFormatException {
        String scolor = reader.readBytes(SCOLOR_LENGTH);
        segment.setSymbolColourFormat(SymbolColour.getEnumValue(scolor));
    }

    private void readSNUM() throws NitfFormatException {
        segment.setSymbolNumber(reader.readBytes(SNUM_LENGTH));
    }

    private void readSROT() throws NitfFormatException {
        segment.setSymbolRotation(reader.readBytesAsInteger(SROT_LENGTH));
    }

    private void readNELUT() throws NitfFormatException {
        numberOfEntriesInLUT = reader.readBytesAsInteger(SYNELUT_LENGTH);
    }

    private void readLUTEntry() throws UnsupportedOperationException, NitfFormatException {
        if (segment.getSymbolType().equals(SymbolType.BITMAP)) {
            if (segment.getSymbolColour().equals(SymbolColour.USE_COLOUR_LUT)) {
                readColourLUTEntry();
                return;
            } else if (segment.getSymbolColour().equals(SymbolColour.USE_GRAYSCALE_LUT)) {
                readGrayScaleLUTEntry();
                return;
            }
        }
        throw new NitfFormatException("Symbol LUT only permitted for Symbol Type B with Symbol Colour G or C.");
    }

    private void readColourLUTEntry() throws NitfFormatException {
        reader.skip(NUM_BYTES_IN_COLOUR_LUT_ENTRY);
    }

    private void readGrayScaleLUTEntry() throws NitfFormatException {
        reader.skip(NUM_BYTES_IN_GRAYSCALE_LUT_ENTRY);
    }

    private void readSXSHDL() throws NitfFormatException {
        symbolExtendedSubheaderLength = reader.readBytesAsInteger(SXSHDL_LENGTH);
    }

    private void readSXSOFL() throws NitfFormatException {
        segment.setExtendedHeaderDataOverflow(reader.readBytesAsInteger(SXSOFL_LENGTH));
    }

    private void readSXSHD() throws NitfFormatException {
        TreCollection extendedSubheaderTREs = parsingStrategy.parseTREs(reader,
                symbolExtendedSubheaderLength - SXSOFL_LENGTH,
                TreSource.SymbolExtendedSubheaderData);
        segment.mergeTREs(extendedSubheaderTREs);
    }
}
