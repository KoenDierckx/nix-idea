package org.nixos.idea.lsp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.LspClient;
import com.intellij.platform.lsp.api.LspIntegrationProvider;
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFileType;
import org.nixos.idea.icon.NixIcons;

@SuppressWarnings("UnstableApiUsage")
public final class NixLspIntegrationProvider implements LspIntegrationProvider {
    @Override
    public void fileOpened(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull LspClientStarter lspClientStarter) {
        if (virtualFile.getFileType() == NixFileType.INSTANCE) {
            NixLspSettings settings = NixLspSettings.getInstance();
            if (settings.isEnabled()) {
                lspClientStarter.ensureClientStarted(new NixLspClientDescriptor(project, settings));
            }
        }
    }

    @Override
    public @NotNull LspClientWidgetItem createWidgetItem(@NotNull LspClient lspClient, @Nullable VirtualFile currentFile) {
        return new LspClientWidgetItem(lspClient, currentFile, NixIcons.FILE, NixLspSettingsConfigurable.class);
    }
}
