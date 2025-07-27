package ct.migratordesktop.beolvaso;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import ct.migratordesktop.swing.AbstractPanel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@org.springframework.stereotype.Component
public class BeolvasoPanel extends AbstractPanel {
	private static final long serialVersionUID = 1L;
	//	@Autowired
	//	private AkkorDataSourceImpl	akkorDataSource;

	public BeolvasoPanel() {
		super();
		//
		final var checkBt = new JButton( "file kiv." );
		checkBt.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				disableButtonPanel();
				//				final var sc = new SchemaReCreator();
				//				textArea.setText( sc.getSql() );

				JFileChooser j = new JFileChooser( FileSystemView.getFileSystemView().getHomeDirectory() );
				int r = j.showOpenDialog( null );
				if ( r == JFileChooser.APPROVE_OPTION ) {
					textArea.setText( j.getSelectedFile().getAbsolutePath() );
				}
				enableButtonPanel();
			}
		} );
		buttonPanel.add( checkBt );
		//
		final var btn = new JButton( "execute" );
		btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				final var actionName = ((JButton)event.getSource()).getText();
				final var result = JOptionPane.showInternalConfirmDialog( null, "Futtatható ?", actionName, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
				if ( result == JOptionPane.YES_OPTION ) {
					final var sql = textArea.getText();
					new Thread( () -> {
						start( actionName );
						//						final var sc = new SchemaReCreator();
						//						sc.executeSql( sql );
						stop();
					} ).start();
				}
			}
		} );
		buttonPanel.add( btn );

		//		final var compareBt = new JButton( "Liquibase On akkor" );
		//		compareBt.addActionListener( new ActionListener() {
		//			@SuppressWarnings("deprecation")
		//			@SneakyThrows
		//			public void actionPerformed( ActionEvent event ) {
		//				final var actionName = ((JButton)event.getSource()).getText();
		//				new Thread( () -> {
		//					start( actionName );
		//					final ClassLoader myself = SchemaPanel.class.getClassLoader();
		//					try (
		//						final ResourceAccessor ra = new ClassLoaderResourceAccessor( myself );
		//						final var conn = akkorDataSource.getDataSourceConfiguration().dataSource().getConnection();
		//						final DatabaseConnection db = new JdbcConnection( conn );
		//						final var lb = new Liquibase( "db/changelog-master.xml", ra, db );) {
		//						lb.clearCheckSums();
		//						lb.update();
		//					}
		//					catch ( Exception e ) {
		//						log.error( "", e );
		//					}
		//					stop();
		//				} ).start();
		//			}
		//		} );
		//		buttonPanel.add( compareBt );
	}

}