package simplenem12;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleNem12ParserImpl implements SimpleNem12Parser {

	private String COMMA = ",";
	private List<MeterRead> meterReads = new ArrayList<MeterRead>();

	@Override
	public Collection<MeterRead> parseSimpleNem12(File simpleNem12File) {

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(simpleNem12File), StandardCharsets.UTF_8));
				Stream<String> lines = br.lines()) {
			lines.skip(1).map(mapToItem).collect(Collectors.toList());
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return meterReads;
	}

	private Function<String, MeterRead> mapToItem = (line) -> {
		String[] p = line.split(COMMA);// a CSV has comma separated lines
		SortedMap<LocalDate, MeterVolume> volumes = null;
		MeterRead item = null;
		if (p.length == 3) { // According to the Sample File only in this case of RecordType 200 the length
								// would be three
			item = new MeterRead(p[1], EnergyUnit.KWH);
			meterReads.add(item);
		} else if (p.length == 4) {
			if (p.length > 3 && (p[3] != null && p[3].trim().length() > 0)) { // According to the Sample File only in this case of RecordType 300 the
																				// length would be greater than three
				if (meterReads.get(meterReads.size() - 1).getVolumes() == null) {// create a new volume Map only when there are no volumes attached to the Meter Reading else use the existing Map
					volumes = new TreeMap<LocalDate, MeterVolume>();
				} else {
					volumes = meterReads.get(meterReads.size() - 1).getVolumes();
				}
				volumes.put(LocalDate.parse(p[1], DateTimeFormatter.BASIC_ISO_DATE),
						new MeterVolume(new BigDecimal(p[2]), Quality.valueOf(p[3])));
				meterReads.get(meterReads.size() - 1).setVolumes(volumes);
			}
		}
		return item;
	};
}
