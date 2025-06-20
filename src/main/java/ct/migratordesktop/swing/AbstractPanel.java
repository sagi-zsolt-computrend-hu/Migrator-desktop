package ct.migratordesktop.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import ct.migratordesktop.util.Stopper;
import ct.migratordesktop.util.logviewer.LogView;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

public abstract class AbstractPanel extends JPanel {
	private static final long	serialVersionUID	= 1L;

	private Stopper						stopper;
	protected ColorPane				textArea;
	protected JPanel					buttonPanel;
	private TimerTask					timerTask;
	private JProgressBar			jProgressBar;
	private int								addValue					= +1;
	private String						actionName				= "";
	private LogView						logView;

	public AbstractPanel() {
		setLayout( new BorderLayout() );
		jProgressBar = new JProgressBar();
		jProgressBar.setMaximum( 10 );
		jProgressBar.setMinimum( 0 );
		jProgressBar.setValue( 0 );
		jProgressBar.setStringPainted( true );
		jProgressBar.setFont( new Font( Font.DIALOG, Font.PLAIN, 14 ) );
		add( jProgressBar, BorderLayout.NORTH );
		textArea();
		buttonArea();
		revalidate();
	}

	protected void start( String actionName ) {
		logView = new LogView();
		logView.init();
		logView.reset();
		this.actionName = actionName;
		textArea.setANSI( "" );
		disableButtonPanel();
		stopper = new Stopper();
		stopper.start();
		timerTask = new TimerTask() {
			public void run() {
				logView( 25 );
				if ( jProgressBar.getMaximum() == jProgressBar.getValue() )
					addValue = -1;
				else if ( jProgressBar.getMinimum() == jProgressBar.getValue() )
					addValue = +1;
				jProgressBar.setValue( jProgressBar.getValue() + addValue );
				jProgressBar.setString( actionName + " (" + stopper.getTime() + ")" );
			}
		};

		Timer timer = new Timer( "Timer" );
		timer.scheduleAtFixedRate( timerTask, 1000, 2 * 1000 );
	}

	protected void disableButtonPanel() {
		buttonPanel.setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
		Arrays.asList( buttonPanel.getComponents() ).forEach( c -> c.setEnabled( false ) );
	}

	private String logView( int lastNum ) {
		final var log = logView.getLog( lastNum );
		textArea.setANSI( log );
		textArea.setCaretPosition( textArea.getDocument().getLength() );
		return log;
	}

	protected void stop() {
		timerTask.cancel();
		logView( 0 );
		final var log = textArea.getText();
		jProgressBar.setValue( jProgressBar.getMinimum() );
		jProgressBar.setString( actionName + " (" + stopper.getTime() + ") "
			+ "Ended log Rows:" + log.chars().filter( ch -> ch == '\n' ).count()
			+ " errors: " + StringUtils.countOccurrencesOf( log, " E " ) );
		enableButtonPanel();
		revalidate();
	}

	protected void enableButtonPanel() {
		Arrays.asList( buttonPanel.getComponents() ).forEach( c -> c.setEnabled( true ) );
		buttonPanel.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
	}

	private void buttonArea() {
		buttonPanel = new JPanel();
		buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.X_AXIS ) );
		add( buttonPanel, BorderLayout.SOUTH );
	}

	private void textArea() {
		textArea = new ColorPane();
		textArea.setBackground( Color.BLACK.brighter() );
		textArea.setForeground( Color.white );
		textArea.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 14 ) );
		JScrollPane jScrollPane = new JScrollPane( textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		add( jScrollPane, BorderLayout.CENTER );
		textArea.setSize( 1222, 690 );

		final var boxPanel = new JPanel();
		boxPanel.setLayout( new BoxLayout( boxPanel, BoxLayout.Y_AXIS ) ); // Create a nested container with FlowLayout for horizontal alignment
		{
			final var jButton = new JButton( "➕" );
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					final var size = textArea.getFont().getSize();
					textArea.setFont( new Font( Font.MONOSPACED, Font.PLAIN, size + 1 ) );
					revalidate();
				}
			} );
			boxPanel.add( jButton );
		}
		{
			final var jButton = new JButton( "➖" );
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					final var size = textArea.getFont().getSize();
					textArea.setFont( new Font( Font.MONOSPACED, Font.PLAIN, size - 1 ) );
					revalidate();
				}
			} );
			boxPanel.add( jButton );
		}
		{
			final var jButton = new JButton( "❌" );
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					textArea.setText( null );
				}
			} );
			boxPanel.add( jButton );
		}
		{
			final var jButton = new JButton( "💾" );
			jButton.addActionListener( new ActionListener() {
				@SneakyThrows
				public void actionPerformed( ActionEvent event ) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setSelectedFile( new File( "Migrator2_" + LocalDateTime.now().format( DateTimeFormatter.ofPattern( "yyyy-MM-dd_HH-mm-ss" ) ) + ".log" ) );
					fileChooser.setCurrentDirectory(new File(System.getProperty( "user.dir" )));
					fileChooser.setDialogTitle( "Log mentése" );
					if ( fileChooser.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION ) {
						File fileToSave = fileChooser.getSelectedFile();
						textArea.setCaretPosition( textArea.getDocument().getLength() );
						final var bytes = (textArea.getText() + "\n" + jProgressBar.getString()).getBytes( StandardCharsets.UTF_8 );
						Files.write( Paths.get( fileToSave.toString() ), bytes,
							StandardOpenOption.TRUNCATE_EXISTING,
							StandardOpenOption.CREATE,
							StandardOpenOption.WRITE );
					}
				}
			} );
			boxPanel.add( jButton );
		}
		{
			final var jButton = new JButton( "⛓" );
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					logView.init();
					textArea.setANSI( logView.getLog() );
				}
			} );
			boxPanel.add( jButton );
			add( boxPanel, BorderLayout.EAST );
		}
	}
}
