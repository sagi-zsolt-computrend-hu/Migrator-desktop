package ct.migratordesktop.tesztdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import ct.migratordesktop.datasources.AbstractDataSource;
import ct.migratordesktop.util.Converters;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TesztDataStep implements Converters, Runnable {
	private AbstractDataSource dataSource;

	public TesztDataStep( AbstractDataSource dataSource ) {
		super();
		this.dataSource = dataSource;
	}

	private final String				tableExportDerby	= " CREATE TABLE \"÷÷\" "
		+ " (\"ID\" BIGINT NOT NULL PRIMARY KEY, \n"
		+ " \"CHAR_1\" VARCHAR(100) , \n"
		+ " \"CHAR_2\" VARCHAR(100) , \n"
		+ " \"DATUM\" DATE, \n"
		+ " \"DECIMAL_12_3\" DECIMAL(12,3) , \n"
		+ " \"LOGIKAI\" DECIMAL(1,0)  ) \n";

	private final String				tableExportOracle	= " CREATE TABLE ÷÷ "
		+ " (ID NUMERIC(32) NOT NULL PRIMARY KEY, \n"
		+ " CHAR_1 VARCHAR2(100 char) , \n"
		+ " CHAR_2 VARCHAR2(100 char) , \n"
		+ " DATUM DATE, \n"
		+ " DECIMAL_12_3 NUMERIC(12,3) , \n"
		+ " LOGIKAI NUMERIC(1,0) )  \n";

	@Setter
	private TesztDataProperties	properties;
	@Setter
	private String							tableName;

	public void run() {
		log.info( "TeszDataGenerator started {}", tableName );
		try {
			dataSource.dropTable( tableName );
			dataSource.execute( (dataSource.isOracle() ? tableExportOracle : tableExportDerby).replace( "÷÷", tableName ) );
			final var insertCommand = "INSERT INTO " + tableName
				+ " (ID,  CHAR_1, CHAR_2,DATUM, DECIMAL_12_3, LOGIKAI) "
				+ " VALUES(?, ?, ?, ?, ?, ?)";
			final var pangramList = getPangramList();
			try (Connection conn = dataSource.getConnection()) {
				conn.setAutoCommit( false );
				try (PreparedStatement st = conn.prepareStatement( insertCommand )) {
					for ( long i = 1; i <= properties.getRecNum(); i++ ) {
						st.setLong( 1, i );
						st.setString( 2, getVarchar100( pangramList ) );
						st.setString( 3, getVarchar100( pangramList ) );
						st.setDate( 4, convertToSqlDate( convertToDate( getRandomLocalDate( "2000.01.01", "2030.12.31" ) ) ) );
						st.setBigDecimal( 5, getRandomBigDecimal( 0.0d, 999999999.999d, 3 ) );
						st.setBoolean( 6, Math.random() < 0.5 );
						st.execute();
						if ( i % properties.getCommit() == 0 ) {
							log.info( "Commit {} {}", tableName, i );
							conn.commit();
						}
					}
				}
				conn.commit();
			}
			catch ( Exception e ) {
				log.error( "Error", e );
			}
		}
		finally {
			log.info( "TeszDataGenerator Ended {} {}", tableName, dataSource.getCount( tableName ) );
		}
	}

	private String getVarchar100( final List<String> pangramList ) {
		String charVar100 = getrandomUUUID( 3, 100 );
		if ( ThreadLocalRandom.current().nextInt( 1, 100 ) > 50 ) {
			charVar100 = pangramList.get( ThreadLocalRandom.current().nextInt( 0, pangramList.size() ) );
		}
		if ( ThreadLocalRandom.current().nextInt( 1, 100 ) > 85 )
			charVar100 = charVar100.toUpperCase();
		return charVar100;
	}

	private String getrandomUUUID( int min, int max ) {
		String ret = UUID.randomUUID().toString();
		while ( ret.length() < max ) {
			ret += UUID.randomUUID().toString();
		}
		return ret.substring( 0, ThreadLocalRandom.current().nextInt( min, max ) );
	}

	private BigDecimal getRandomBigDecimal( double min, double max, int scale ) {
		double ret = ThreadLocalRandom.current().nextDouble( min, max );
		return new BigDecimal( ret ).setScale( scale, RoundingMode.CEILING );
	}

	private LocalDate getRandomLocalDate( String min, String max ) {
		LocalDate minDay = LocalDate.parse( min, DateTimeFormatter.ofPattern( "yyyy.MM.dd" ) );
		LocalDate maxDay = LocalDate.parse( max, DateTimeFormatter.ofPattern( "yyyy.MM.dd" ) );
		long randomDay = ThreadLocalRandom.current().nextLong( minDay.toEpochDay(), maxDay.toEpochDay() );
		LocalDate randomDate = LocalDate.ofEpochDay( randomDay );
		return randomDate;
	}

	private LocalDateTime getRandomLocalDateTime( String min, String max ) {
		LocalDateTime minLocalDateTime = LocalDateTime.parse( min, DateTimeFormatter.ofPattern( "yyyy.MM.dd HH:mm:ss" ) );
		LocalDateTime maxLocalDateTime = LocalDateTime.parse( max, DateTimeFormatter.ofPattern( "yyyy.MM.dd HH:mm:ss" ) );
		long randomMillisSinceEpoch = ThreadLocalRandom.current().nextLong( convertToDate( minLocalDateTime ).getTime(), convertToDate( maxLocalDateTime ).getTime() );

		return convertToLocalDateTime( new Date( randomMillisSinceEpoch ) );
	}

	private List<String> getPangramList() {
		return Arrays.asList( "árvíztűrő tükörfúrógép", "bőszájú körülíróművész", "csúszdázó műbőr különítmény", "húsz kábító fűrész őrködik, üt", "fűsújtó, sárgördítő ütés", "gyümölcsvédő ágyúfűnyíró", "háztűznézőügynök-búsító", "jóhírű tüzérágyúöntő", "jóízű félárú sütőtök", "jött árvíz, tűzvész, rút gümőkór", "kövér fülű sítúrázó nő",
			"különálló műútépítő", "nyúlfülvágó térközsűrítő", "ötágú ütőműbénító", "öt szép szűzlány őrült írót nyúz", "tégy úgy őrült, már bűvölsz, hódíts", "tíz büdös légy húsz műcsótányt főz", "több hűtőházból kértünk színhúst", "tűzön tíz órát égő nyúlfül", "sós húst sütsz tán, vízköpő szűcsné", "szénrázúdító fűtőküldönc",
			"szőrösfülű vén sírásó úr", "túlkábító műrémölőfül", "tüskéshátú kígyóbűvölő", "tűrő társ békít, s újból örül", "új füvön csábító kéjnőt gyűr", "úrnőm, gyümölcsízű rágót végy", "úszójárműkürt-vészöblítő", "üldögélő műújságíró", "vájt fülű bíró két döntőt fújt", "zártkörű nőújító ülés", "szélütött űrújságírónő",
			"adjál elém pikírt, bohó költőt, unt, bús tüzűt", "a fűrészbolt-felvigyázó őrön új kulcsszíj csüng", "hálóűrbe fölül lő, bosszús kapusszív vérzik", "vízi hosszúbukó-alámerülő működés", "külvízen úszó szárazjégtörő burkolt kisjármű", "csábító kéjnőt új füvön gyűrt, gyűlésükön újból nőt kívánt",
			"húsz ősz bíró bűbájtörvényt ül: vén nőt kínzó lángú tűz süssön", "új fűszárító gőzgépünkön kávészínű lódöghúst főzünk", "új-vitorlázórepülőgép-altípus-röptű", "fürge rőt róka túlszökik zsíros étkű kutyán" );
	}

}