package ct.migratordesktop.swing;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ct.migratordesktop.datasources.DataSourcePanel;
import ct.migratordesktop.exportal.ExportPanel;
import ct.migratordesktop.importal.ImportPanel;
import ct.migratordesktop.schema.SchemaPanel;
import ct.migratordesktop.tesztdata.TesztDataPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class MigratorJframe extends JFrame implements CommandLineRunner, Ordered {
	private static final long	serialVersionUID	= 1L;
	@Autowired
	private DataSourcePanel		dataSourcePanel;

	@Autowired
	private ExportPanel				exportPanel;

	@Autowired
	private ImportPanel				importPanel;

	@Autowired
	private TesztDataPanel		tesztDataPanel;

	@Autowired
	private SchemaPanel		schemaPanel;

//	@Autowired
//	private InitDbPanel		initDbPanel;
	
	
	public MigratorJframe() throws HeadlessException {
		setTitle( "DataSources [" + System.getProperty( "user.dir" ) + "]"  );
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void run( String... args ) throws Exception {
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		setSize( 1224, 600 );
		var jMenuBar = new JMenuBar();
		setJMenuBar( jMenuBar );
		jMenuBar.setVisible( true );
		{
			var mi = new JMenuItem( "DataSources" );
			mi.addActionListener(
				new ActionListener() {
					public void actionPerformed( ActionEvent event ) {
						setContentPane( dataSourcePanel );
						dataSourcePanel.revalidate();
						revalidate();
					}
				} );
			jMenuBar.add( mi );
		}
		{
			var mi = new JMenuItem( "Export" );
			mi.addActionListener(
				new ActionListener() {
					public void actionPerformed( ActionEvent event ) {
						setContentPane( exportPanel );
						dataSourcePanel.revalidate();
						revalidate();
					}
				} );
			jMenuBar.add( mi );
		}
		{
			var mi = new JMenuItem( "Teszt Data" );
			mi.addActionListener(
				new ActionListener() {
					public void actionPerformed( ActionEvent event ) {
						setContentPane( tesztDataPanel );
						tesztDataPanel.revalidate();
						revalidate();
					}
				} );
			jMenuBar.add( mi );
		}
		{
			var mi = new JMenuItem( "Import" );
			mi.addActionListener(
				new ActionListener() {
					public void actionPerformed( ActionEvent event ) {
						setContentPane( importPanel );
						importPanel.revalidate();
						revalidate();
					}
				} );
			jMenuBar.add( mi );
		}
		{
			var mi = new JMenuItem( "Schema" );
			mi.addActionListener(
				new ActionListener() {
					public void actionPerformed( ActionEvent event ) {
						setContentPane( schemaPanel );
						schemaPanel.revalidate();
						revalidate();
					}
				} );
			jMenuBar.add( mi );
		}
//		{
//			var mi = new JMenuItem( "InitDb" );
//			mi.addActionListener(
//				new ActionListener() {
//					public void actionPerformed( ActionEvent event ) {
//						setContentPane( initDbPanel );
//						initDbPanel.revalidate();
//						revalidate();
//					}
//				} );
//			jMenuBar.add( mi );
//		}

		setVisible( true );
	}
}