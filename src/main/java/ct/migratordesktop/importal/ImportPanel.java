package ct.migratordesktop.importal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import ct.migratordesktop.swing.AbstractPanel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Slf4j
@org.springframework.stereotype.Component
public class ImportPanel extends AbstractPanel {
	private static final long	serialVersionUID	= 1L;
	@Lazy
	@Autowired
	private ImportServiceImpl	importService;

	public ImportPanel() {
		super();
		//
		final var btn = new JButton( "Import (Derby->medkontroll)" );
		btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				final var actionName = ((JButton)event.getSource()).getText();
				final var result = JOptionPane.showInternalConfirmDialog( null, "Futtatható ?", actionName, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
				if ( result == JOptionPane.YES_OPTION )
					new Thread( () -> {
						start( actionName );
						importService.importal();
						stop();
					} ).start();
			}
		} );
		buttonPanel.add( btn );

		final var compareBt = new JButton( "Compare" );
		compareBt.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				final var actionName = ((JButton)event.getSource()).getText();
				new Thread( () -> {
					start( actionName );
					importService.compare();
					stop();
				} ).start();
			}
		} );
		buttonPanel.add( compareBt );
	}

}
//				log.error( UUID.randomUUID().toString() );
//			return logview.getLog();

//						for ( int i = 0; i < 101; i++ ) {
//							log.info( UUID.randomUUID().toString() );
//							LogView logView = new LogView( config );
//							logView.init();
//							textArea.setANSI( logView.getLog() );
//							revalidate();
//							try {
//								Thread.sleep( 500 );
//							}
//							catch ( InterruptedException e ) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//			TimerTask task2 = new TimerTask() {
//		    public void run() {
//					LogView logView = new LogView( config );
//					logView.init();
//					textArea.setANSI( logView.getLog() );
//							revalidate();
//
//		    }
//		};
//		Timer timer2 = new Timer("Timer");
//		timer2.scheduleAtFixedRate(task2, 10, 5  * 1000);

//				TimerTask task1 = new TimerTask() {
//					public void run() {
//						LogView logView = new LogView( config );
//						logView.init();
//						final var log = logView.getLog();
//						textArea.setANSI( log );
//						textArea.setCaretPosition(textArea.getDocument().getLength());
//						//textArea.setCaretPosition( log.length() );
//						//revalidate();
//					}
//				};
//
//				buttonPanel.setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
//				for ( java.awt.Component cp : buttonPanel.getComponents() ) {
//					cp.setEnabled( false );
//				}
//				Timer timer = new Timer( "Timer" );
//				
//				timer.scheduleAtFixedRate( task1, 10, 2 * 1000 );

//				Executors.newSingleThreadExecutor().execute( new Runnable() {
//					@Override
//					public void run() {
//						stageDataSource.xx();
//						timer.cancel();
//						buttonPanel.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
//						for ( java.awt.Component cp : buttonPanel.getComponents() ) {
//							cp.setEnabled( true );
//						}
//					}
//				} );
//					timerTask.cancel();
//											buttonPanel.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
//											for ( java.awt.Component cp : buttonPanel.getComponents() ) {
//												cp.setEnabled( true );
//											}