package ct.migratordesktop.export;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ct.migratordesktop.datasources.derby.DerbyDataSourceConfiguration;
import ct.migratordesktop.util.Converters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExportServiceImpl implements Converters {
	@Autowired
	private DerbyDataSourceConfiguration	derbyDataSourceConfiguration;

	private Path													path	= null;

	@SneakyThrows
	void export() {
		Properties intezmenyProperties = new Properties();
		try {
			path = Paths.get( derbyDataSourceConfiguration.dataSource().getConnection().getMetaData().getURL()
				.replace( "jdbc:derby:directory://", "" ) ).getParent();
			path = Paths.get( path.toString(), "export" );
			if ( !Files.exists( Paths.get( path.toString() ) ) )
				Files.createDirectories( Paths.get( path.toString() ) );
			final var filePath = Paths.get( path.toString(), "intezmeny.properties" );
			try (var inputStream = Files.newInputStream( filePath );
				var reader = new InputStreamReader( inputStream, StandardCharsets.UTF_8 )) {
				intezmenyProperties.load( reader );
			}
		}
		catch ( Exception e ) {
			log.error( "Error", e );
		}

		final var exector = Executors.newFixedThreadPool( 1 );
		exp( exector, new StepRecord( "ECOSTAT_COLUMS", """
			select * From ECOSTAT_COLUMS
						""" ) );

		exp( exector, new StepRecord( "ECOSTAT_PRIMARY_KEYS", """
			select * From ECOSTAT_PRIMARY_KEYS
						""" ) );

		exp( exector, new StepRecord( "MK_PARAMETEREK", """
			select * From MK_PARAMETEREK
						""" ) );

		exp( exector, new StepRecord( "aradat_torzs",
			"""
				SELECT EV  ,HONAP,GOND_PONT ,PCR_LIM_ATHOZ,VIZITDIJ_KOMP,MSZSZ_NO,NEMET_LIM_SZERZ,LABOR_LIM_ATHOZ,HBCS_SULYSZAM,NEMET_PONT_ALAP,FOG_PONT    ,PCR_LPER2,PCR_LPER1,LABOR_FT3 ,NEMET_LIM_ATHOZ,KISZOLG_FIXAR,PCR_VPER3,LABOR_FT2 ,PCR_LPER3,LABOR_FT1 ,PCR_VPER1,KRONIKUS_SULY_ERTEKE,PCR_VPER2,LABOR_LIM_SZERZ,INT_HOSSZU_NAP_ERT,DIAGN_FIXAR,NAP1_VPER3,NAP1_VPER2,NEMET_LPER3,NEMET_LIMIT,NEMET_LPER1,NEMET_LPER2,PCR_PONT_ALAP,NAP1_ERTEK  ,LABOR_PONT,KRON_SULY2 ,HBCS_SULY_ALAP,HBCS_LIMIT,HBCS_VPER2,HBCS_VPER3,NAP1_ALAP,HBCS_VPER1,NAP1_LIM_SZERZ,HBCS_LIM_SZERZ,HOSSZU_NAP_ERTEKE,NAP1_VPER1,LABOR_VPER1,LABOR_VPER2,LABOR_VPER3,NAP1_LPER1,NAP1_LPER2,HBCS_LIM_ATHOZ,NAP1_LPER3,NEMET_VPER2,NEMET_VPER1,HBCS_LPER1,NEMET_VPER3,HBCS_LPER3,HBCS_LPER2,LABOR_PONT_ALAP,NEMET_PONT_ERTEKE,PCR_LIMIT,PCR_LIM_SZERZ,NAP1_LIM_ATHOZ,PCR_PONT  ,CT_PONT     ,LABOR_LPER1,NAP1_LIMIT,LABOR_LPER2,LABOR_LPER3,LABOR_LIMIT,MSZSZ_GY
				 FROM APP.MK_ARTORZS ORDER BY ev,honap
						""" ) );

		exp( exector, new StepRecord( "fokonyvi_szamok", """
			SELECT SZAMLASZAM ,MEGNEVEZES ,TELJESITES FROM APP.MK_FOKONYV WHERE EV = ? 
				""", intezmenyProperties.get( "EV" ) ) );

		exp( exector, new StepRecord( "szerv_csop", """
			SELECT SZERV_CSOPORT_KOD,SZERV_CSOPORT_NEVE, FAJTA  
			 FROM APP.MK_SZERVEZETICSOPORTOK WHERE SZERV_CSOPORT_KOD NOT LIKE 'AEEK_%' ORDER BY SZERV_CSOPORT_KOD
				""" ) );

		exp( exector, new StepRecord( "szerv_csop_ossz", """
		SELECT SZERV_CSOPORT_KOD,SZERV_EGYSEG_KOD,ERV_KEZD,ERV_VEGE  FROM APP.MK_SZCSOPOK_SZERVEZETEK 
		 WHERE ? BETWEEN MK_SZCSOPOK_SZERVEZETEK.ERV_KEZD AND MK_SZCSOPOK_SZERVEZETEK.ERV_VEGE
				""", intezmenyProperties.get( "EV" ) + "01" ) );
		
		exp( exector, new StepRecord( "szervezeti_finanszirozasi_kodok", """
			SELECT DISTINCT SZERV_EGYSEG_KOD,OEP_KOD  FROM APP.MK_SZERVEZETI_OEPKODOK WHERE EV IN (?)
				""", intezmenyProperties.get( "EV" ) ) );

		exp( exector, new StepRecord( "szervezeti_fokonyviszamok", """
				SELECT * FROM APP.MK_SZERVEZETI_FOKONYVISZAMOK
				 WHERE (EV IN (?)) AND (HONAP IN ('01')) 
				""", intezmenyProperties.get( "EV" ) ) );
		
		exp( exector, new StepRecord( "szervezeti_fokonyviszamok_masodlagos", """
		SELECT * FROM APP.MK_SZERVEZET_REND_FK WHERE EV IN (?) 
			""", intezmenyProperties.get( "EV" ) ) );
		
		exp( exector, new StepRecord( "szervezeti_torzs", """
		SELECT EV  ,HONAP,OSZTAS_TIPUS,KISSZEM_SZAMA,MSZSZ_TIPUS,HONAP,HAVI_ORA,RAKT_ID,KRON_TIPUS,EGYEB_SZAMA,KOZP_OSZTHAT,FOKONYVI_SZAM_DARAB,FIX_AR,TIPUS,MUKODO_AGYAK,HETI_ORA,KULSO_SZOLG,SZERV_EGYSEG_KOD,ELLATASI_TERULET,FAJTA,SZERZODOTT_AGYAK,ORVOSOK_SZAMA,BELSO_KOD,BEVETELI_FOKONYVI_SZAM_DARAB,KRON_SZORZO,SZERV_EGYSEG_NEVE,SZAKDOLGOZOK_SZAMA,BEV_OSZTAS_TIPUS,'' AS FIN_KOD
		 FROM APP.MK_SZERVEZETTORZS WHERE EV IN (?)
			""", intezmenyProperties.get( "EV" ) ) );

		exp( exector, new StepRecord( "afa", """
			SELECT AFA_SZAZALEK,FKSZAM FOKONYVI_SZAM FROM APP.MK_AFA_BEALLIT WHERE EV IN (?)
			""", intezmenyProperties.get( "EV" ) ) );
		
		exp( exector, new StepRecord( "aradat_torzs", """
			SELECT *		 FROM APP.MK_ARTORZS ORDER BY ev,honap
			""" ) );
		
		exp( exector, new StepRecord( "koltseg_arsema", """
			SELECT * FROM APP.MK_ARKATA WHERE VONEV IN (?)
			""" , intezmenyProperties.get( "EV" )) );

		exector.shutdown();
		exector.awaitTermination( 100, TimeUnit.HOURS );
	}

	private void exp( ExecutorService executor, StepRecord stepRecord ) {
		var step = new ExportStep( derbyDataSourceConfiguration );
		step.setStepRecord( stepRecord );
		step.setPath( path );
		executor.submit( step );
	}

	record StepRecord( String fileName, String sql, Object... params ) {
	}

}
