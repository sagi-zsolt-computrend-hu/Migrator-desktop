package ct.migratordesktop.exportal;

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
public class ExportPanel extends AbstractPanel {
	private static final long	serialVersionUID	= 1L;
	@Lazy
	@Autowired
	private ExportServiceImpl	exportService;

	public ExportPanel() {
		super();
		//
		final var exportBt = new JButton( "Export  (Ecostat -> Export)" );
		exportBt.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				final var actionName = ((JButton)event.getSource()).getText();
				final var result = JOptionPane.showInternalConfirmDialog( null, "Futtatható ?", actionName, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
				if ( result == JOptionPane.YES_OPTION )
					new Thread( () -> {
						start( actionName );
						exportService.exportal();
						stop();
					} ).start();
			}
		} );
		buttonPanel.add( exportBt );

		final var compareBt = new JButton( "Compare" );
		compareBt.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				final var actionName = ((JButton)event.getSource()).getText();
				new Thread( () -> {
					start( actionName );
					exportService.compare();
					stop();
				} ).start();
			}
		} );
		buttonPanel.add( compareBt );
	}

}