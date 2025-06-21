package ct.migratordesktop.datasources;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import ct.migratordesktop.datasources.ecostat.EcoStatDataSourceImpl;
import ct.migratordesktop.datasources.export.ExportDataSourceImpl;
import ct.migratordesktop.datasources.medkontroll.MedkontrollDataSourceImpl;
import ct.migratordesktop.swing.AbstractPanel;
import ct.migratordesktop.util.Converters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

@Slf4j
@org.springframework.stereotype.Component
public class DataSourcePanel extends AbstractPanel implements Converters {
	private static final long					serialVersionUID	= 1L;

	//	@Lazy
	//	@Autowired
	//	private ExportProperties			exportProperties;
	//
	//	@Lazy
	//	@Autowired
	//	private ExportProperties			importProperties;
	//
	//	
	//	@Lazy
	//	@Autowired
	//	private AlapDataSourceImpl		alapDataSource;
	//	@Lazy
	//	@Autowired
	//	private ExportDataSourceImpl	exportDataSource;
	//
	@Lazy
	@Autowired
	private EcoStatDataSourceImpl			ecoStatDataSource;

	@Lazy
	@Autowired
	private ExportDataSourceImpl			exportDataSource;

	@Lazy
	@Autowired
	private MedkontrollDataSourceImpl	medkontrollDataSource;

//	@Lazy
//	@Autowired
//	private AkkorDataSourceImpl				akkorDataSource;

	@Lazy
	@Autowired
	private Environment								environment;

	public DataSourcePanel() {
		super();
		//		{
		//			final var jButton = new JButton( "alap" );
		//			jButton.addActionListener( new ActionListener() {
		//				public void actionPerformed( ActionEvent event ) {
		//					try {
		//						disableButtonPanel();
		//						//textArea.appendANSI( alapDataSource.getConnInfo() );
		//					}
		//					finally {
		//						enableButtonPanel();
		//					}
		//				}
		//			} );
		//			buttonPanel.add( jButton );
		//		}
		{
			final var jButton = new JButton( "Export (Derby)" );
			jButton.addActionListener( new ActionListener() {

				public void actionPerformed( ActionEvent event ) {
					try {
						textArea.appendANSI( exportDataSource.getDataSourceInfo() );
					}
					finally {
						enableButtonPanel();
					}
				}
			} );
			buttonPanel.add( jButton );
		}
		{
			final var jButton = new JButton( "Ecostat (Readonly)" );
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					try {
						disableButtonPanel();
						textArea.appendANSI( ecoStatDataSource.getDataSourceInfo() );
					}
					finally {
						enableButtonPanel();
					}
				}
			} );
			buttonPanel.add( jButton );
		}
		{
			final var jButton = new JButton( "Medkontroll (Oracle)" );
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					try {
						disableButtonPanel();
						textArea.appendANSI( medkontrollDataSource.getDataSourceInfo() );
					}
					finally {
						enableButtonPanel();
					}
				}
			} );
			buttonPanel.add( jButton );
		}
//		{
//			final var jButton = new JButton( "Akkor" );
//			jButton.addActionListener( new ActionListener() {
//				public void actionPerformed( ActionEvent event ) {
//					try {
//						disableButtonPanel();
//						textArea.appendANSI( akkorDataSource.getDataSourceInfo() );
//					}
//					finally {
//						enableButtonPanel();
//					}
//				}
//			} );
//			buttonPanel.add( jButton );
//		}
		{
			final var jButton = new JButton( "Check Derby" );
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					try {
						disableButtonPanel();
						textArea.appendANSI( exportDataSource.exportCheck() );
					}
					finally {
						enableButtonPanel();
					}
				}
			} );
			buttonPanel.add( jButton );
		}
		{
			final var jButton = new JButton( "Properties" );
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					try {
						textArea.appendANSI( getSysInfo() );
					}
					finally {
						enableButtonPanel();
					}

				}
			} );
			buttonPanel.add( jButton );
		}
		{
			final var jButton = new JButton( "alap Exec Sql" );
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					final var actionName = ((JButton)event.getSource()).getText();
					new Thread( () -> {
						try {
							var commands = textArea.getText().split( ";" );
							start( actionName );
							for ( int i = 0; i < commands.length; i++ ) {
								var command = commands[i];
								log.info( "Executing {}", command );
								//alapDataSource.execute( command );
							}
						}
						finally {
							stop();
						}
					} ).start();
				}
			} );
			buttonPanel.add( jButton );
		}
	}

	private String getSysInfo() {
		var ret = "SysInfo\n";
		ret += "  user.name             : " + System.getProperty( "user.name" ) + "\n";
		ret += "  user.home             : " + System.getProperty( "user.home" ) + "\n";
		ret += "  user.dir              : " + System.getProperty( "user.dir" ) + "\n";
		ret += "  java.vendor.version   : " + System.getProperty( "java.vendor.version" ) + "\n";
		ret += "  spring.config.location: " + System.getProperty( "spring.config.location" ) + "\n";
		ret += "  logging.config        : " + System.getProperty( "logging.config" ) + "\n";
		ret += "  java.io.tmpdir        : " + System.getProperty( "java.io.tmpdir" ) + "\n";

		//		ret += exportProperties.toString() + "\n";
		//		ret += importProperties.toString() + "\n";
		return ret;
	}

	protected String getSource() {//@formatter:off
		return """
		""";//@formatter:on
	}
}