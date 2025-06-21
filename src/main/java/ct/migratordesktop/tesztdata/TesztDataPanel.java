package ct.migratordesktop.tesztdata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import ct.migratordesktop.swing.AbstractPanel;
import ct.migratordesktop.util.Converters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Slf4j
@org.springframework.stereotype.Component
public class TesztDataPanel extends AbstractPanel implements Converters {
	private static final long		serialVersionUID	= 1L;
	@Lazy
	@Autowired
	public TesztDataServiceImpl	tesztDataService;

	public TesztDataPanel() {
		super();
//		{
//			final var jButton = new JButton( "Akkor" );
//			jButton.addActionListener( new ActionListener() {
//				public void actionPerformed( ActionEvent event ) {
//					final var actionName = ((JButton)event.getSource()).getText();
//					start( actionName );
//					new Thread( () -> {
//						tesztDataService.generateAkkor();
//						stop();
//					} ).start();
//				}
//			} );
//			buttonPanel.add( jButton );
//		}
		{
			final var jButton = new JButton( "Derby" );
			jButton.addActionListener( new ActionListener() {
				@SneakyThrows
				public void actionPerformed( ActionEvent event ) {
					final var actionName = ((JButton)event.getSource()).getText();
					start( actionName );
					new Thread( () -> {
						tesztDataService.generateExport();
						stop();
					} ).start();
				}
			} );
			buttonPanel.add( jButton );
		}
		{
			final var jButton = new JButton( "medkontroll(Oracle)" );
			jButton.addActionListener( new ActionListener() {
				@SneakyThrows
				public void actionPerformed( ActionEvent event ) {
					final var actionName = ((JButton)event.getSource()).getText();
					start( actionName );
					new Thread( () -> {
						tesztDataService.generateMedkontroll();
						stop();
					} ).start();
				}
			} );
			buttonPanel.add( jButton );
		}
	}

	protected String getSource() {//@formatter:off
		return """
DROP TABLE NEWTABLE CASCADE CONSTRAINTS PURGE;
purge recyclebin;
CREATE TABLE NEWTABLE 
  (ID NUMBER(38,0) NOT NULL PRIMARY KEY, 
   RAND_NUMBER NUMBER(9,3), 
   RAND_STRING_1 VARCHAR2(100),
   RAND_STRING_2 VARCHAR2(100),
   RAND_STRING_3 VARCHAR2(100), 
   RAND_DATE DATE, 
   RAND_BOOLEAN NUMBER(1,0)
  );
INSERT INTO newtable
  SELECT LEVEL as id,
  round(dbms_random.value(1,999999),3) AS rand_number,
  dbms_random.string('A', dbms_random.value(10,97)) AS rand_string_1,
  dbms_random.string('A', dbms_random.value(10,97)) AS rand_string_2,
  dbms_random.string('A', dbms_random.value(10,97)) AS rand_string_3,
  TO_TIMESTAMP('2000.01.01', 'YYYY.MM.DD') + dbms_random.value(0, (TO_DATE('2022.12.31', 'YYYY.MM.DD') - TO_DATE('2000.01.01', 'YYYY.MM.DD')+1)) AS rand_date,
  round(dbms_random.value(0,1),0) AS rand_boolean
	from dual connect by level <= 1234500
		""";//@formatter:on
	}
}