package ct.migratordesktop.export;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import ct.migratordesktop.swing.AbstractPanel;
import ct.migratordesktop.util.Converters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Slf4j
@org.springframework.stereotype.Component
public class ExportPanel extends AbstractPanel implements Converters {
	private static final long	serialVersionUID	= 1L;
	@Lazy
	@Autowired
	public ExportServiceImpl	exportService;

	public ExportPanel() {
		super();
		{
			final var jButton = new JButton( "export" );
			jButton.addActionListener( new ActionListener() {
				@SneakyThrows
				public void actionPerformed( ActionEvent event ) {
					final var actionName = ((JButton)event.getSource()).getText();
					final var result = JOptionPane.showInternalConfirmDialog( null, "Futtatható ?", actionName, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
					if ( result == JOptionPane.YES_OPTION )
						new Thread( () -> {
							start( actionName );
							exportService.export();
							stop();
						} ).start();
				}
			} );
			buttonPanel.add( jButton );
		}
	}
}