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
		Properties exportProperties = new Properties();
		try {
			path = Paths.get( derbyDataSourceConfiguration.dataSource().getConnection().getMetaData().getURL()
				.replace( "jdbc:derby:directory://", "" ) ).getParent();
			path = Paths.get( path.toString(), "export" );
			if ( !Files.exists( Paths.get( path.toString() ) ) )
				Files.createDirectories( Paths.get( path.toString() ) );
			final var filePath = Paths.get( path.toString(), "_export.properties" );
			try (var inputStream = Files.newInputStream( filePath );
				var reader = new InputStreamReader( inputStream, StandardCharsets.UTF_8 )) {
				exportProperties.load( reader );
			}
		}
		catch ( Exception e ) {
			log.error( "Error", e );
		}
		final var exector = Executors.newFixedThreadPool( 1 );

		exp( exector, new StepRecord( "aradat_torzs",
			"""
				SELECT EV  ,HONAP,GOND_PONT ,PCR_LIM_ATHOZ,VIZITDIJ_KOMP,MSZSZ_NO,NEMET_LIM_SZERZ,LABOR_LIM_ATHOZ,HBCS_SULYSZAM,NEMET_PONT_ALAP,FOG_PONT    ,PCR_LPER2,PCR_LPER1,LABOR_FT3 ,NEMET_LIM_ATHOZ,KISZOLG_FIXAR,PCR_VPER3,LABOR_FT2 ,PCR_LPER3,LABOR_FT1 ,PCR_VPER1,KRONIKUS_SULY_ERTEKE,PCR_VPER2,LABOR_LIM_SZERZ,INT_HOSSZU_NAP_ERT,DIAGN_FIXAR,NAP1_VPER3,NAP1_VPER2,NEMET_LPER3,NEMET_LIMIT,NEMET_LPER1,NEMET_LPER2,PCR_PONT_ALAP,NAP1_ERTEK  ,LABOR_PONT,KRON_SULY2 ,HBCS_SULY_ALAP,HBCS_LIMIT,HBCS_VPER2,HBCS_VPER3,NAP1_ALAP,HBCS_VPER1,NAP1_LIM_SZERZ,HBCS_LIM_SZERZ,HOSSZU_NAP_ERTEKE,NAP1_VPER1,LABOR_VPER1,LABOR_VPER2,LABOR_VPER3,NAP1_LPER1,NAP1_LPER2,HBCS_LIM_ATHOZ,NAP1_LPER3,NEMET_VPER2,NEMET_VPER1,HBCS_LPER1,NEMET_VPER3,HBCS_LPER3,HBCS_LPER2,LABOR_PONT_ALAP,NEMET_PONT_ERTEKE,PCR_LIMIT,PCR_LIM_SZERZ,NAP1_LIM_ATHOZ,PCR_PONT  ,CT_PONT     ,LABOR_LPER1,NAP1_LIMIT,LABOR_LPER2,LABOR_LPER3,LABOR_LIMIT,MSZSZ_GY
				 FROM APP.MK_ARTORZS ORDER BY ev,honap
						""" ) );

		exp( exector, new StepRecord( "fokonyvi_szamok", """
			SELECT * FROM APP.MK_FOKONYV WHERE EV = ? 
			 ORDER BY SZAMLASZAM
				""", exportProperties.get( "EV" ) ) );
//  felülvizsgálni ezt az aeek  ??
		exp( exector, new StepRecord( "szerv_csop", """
			SELECT * FROM APP.MK_SZERVEZETICSOPORTOK WHERE SZERV_CSOPORT_KOD NOT LIKE 'xAEEK_%'
			  ORDER BY SZERV_CSOPORT_KOD
				""" ) );

		exp( exector, new StepRecord( "szerv_csop_ossz", """
		SELECT * FROM APP.MK_SZCSOPOK_SZERVEZETEK WHERE ERV_KEZD like ? 
		 ORDER BY SZERV_CSOPORT_KOD,SZERV_EGYSEG_KOD,ERV_KEZD,ERV_VEGE
				""", exportProperties.get( "EV" ) + "%" ) );

		exp( exector, new StepRecord( "szervezeti_finanszirozasi_kodok", """
			SELECT * FROM APP.MK_SZERVEZETI_OEPKODOK WHERE EV IN (?)
			ORDER BY HONAP,SZERV_EGYSEG_KOD,OEP_KOD 
				""", exportProperties.get( "EV" ) ) );

		exp( exector, new StepRecord( "szervezeti_fokonyviszamok", """
				SELECT * FROM APP.MK_SZERVEZETI_FOKONYVISZAMOK WHERE (EV IN (?)) 
				 ORDER BY HONAP,SZERV_EGYSEG_KOD,FOKONYVI_SZAM,TIPUS
				""", exportProperties.get( "EV" ) ) );
		
		exp( exector, new StepRecord( "szervezeti_fokonyviszamok_masodlagos", """
		SELECT * FROM APP.MK_SZERVEZET_REND_FK WHERE EV IN (?) 
		 ORDER BY HONAP,SZERV_EGYSEG_KOD,FKSZAM,TIPUS
			""", exportProperties.get( "EV" ) ) );
		
		exp( exector, new StepRecord( "szervezeti_torzs", """
		SELECT * FROM APP.MK_SZERVEZETTORZS WHERE EV IN (?)
		 ORDER BY HONAP,SZERV_EGYSEG_KOD,TIPUS
			""", exportProperties.get( "EV" ) ) );

		exp( exector, new StepRecord( "afa", """
			SELECT * FROM APP.MK_AFA_BEALLIT WHERE EV IN (?)
			 ORDER BY HONAP,FKSZAM,AFA_SZAZALEK
			""", exportProperties.get( "EV" ) ) );
		
		exp( exector, new StepRecord( "aradat_torzs", """
			SELECT * FROM APP.MK_ARTORZS WHERE EV IN (?) 
			 ORDER BY ev,honap
			""", exportProperties.get( "EV" ) ) );
		
		exp( exector, new StepRecord( "koltseg_arsema", """
			SELECT * FROM APP.MK_ARKATA WHERE VONEV IN (?)
			 ORDER BY SEMAKOD,SZERV_EGYS_KOD
			""" , exportProperties.get( "EV" )) );

		exp( exector, new StepRecord( "mennyisegi_egyseg", """
			SELECT n.*,i.nev AS nev_aeek, i.tipus FROM APP.MK_MENNY_EGYSEG n left join MK_MEGYS_AEEK i ON n.KOD_AEEK = i.KOD AND i.EV = n.EV
			 ORDER BY n.ev,n.KOD
			""" ));

		exp( exector, new StepRecord( "SZERVEZET_REND2", """
			SELECT * FROM APP.MK_SZERVEZET_REND2 WHERE EV IN (?)
			""" , exportProperties.get( "EV" )) );

		exp( exector, new StepRecord( "SZERVEZET_RENDEL", """
			SELECT * FROM APP.MK_SZERVEZET_RENDEL WHERE EV IN (?)
			""" , exportProperties.get( "EV" )) );
		
		exp( exector, new StepRecord( "NEAK_SZERZODOTTEK", """
			SELECT * FROM APP.MK_OEPKOD_TORZS WHERE EV IN (?)
			""" , exportProperties.get( "EV" )) );
		
		exp( exector, new StepRecord( "MK_FELDOLG_INPUT", """
			SELECT * FROM APP.MK_FELDOLG_INPUT 
			""" ) );

		exp( exector, new StepRecord( "MK_PARAMETEREK", """
			SELECT * FROM APP.MK_PARAMETEREK 
			""" ) );

		exp( exector, new StepRecord( "KISZOLG_EGYS_VET_ALAP", """
			SELECT * FROM APP.MK_VETALAP WHERE EV IN (?)
			""" , exportProperties.get( "EV" )) );
		
		exp( exector, new StepRecord( "FOKONYV_NAPLO", """
			SELECT * FROM APP.MK_NAPLO WHERE YEAR(DATUM) = ? and MONTH(DATUM) = 9
			""" , exportProperties.get( "EV" )) );
		
		exp( exector, new StepRecord( "F_NAPLO", """
			SELECT * FROM APP.F_NAPLO 
			""" ) );
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

//		exp( exector, new StepRecord( "ECOSTAT_COLUMS", """
//			select * From ECOSTAT_COLUMS
//						""" ) );

//		exp( exector, new StepRecord( "ECOSTAT_PRIMARY_KEYS", """
//			select * From ECOSTAT_PRIMARY_KEYS
//						""" ) );

//		exp( exector, new StepRecord( "MK_PARAMETEREK", """
//			select * From MK_PARAMETEREK
//						""" ) );