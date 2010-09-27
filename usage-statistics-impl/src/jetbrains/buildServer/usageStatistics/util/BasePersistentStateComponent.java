/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.usageStatistics.util;

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public abstract class BasePersistentStateComponent extends BuildServerAdapter {
  @NotNull protected final SBuildServer myServer;
  @NotNull private final File myFile;

  public BasePersistentStateComponent(@NotNull final SBuildServer server, @NotNull final ServerPaths serverPaths) {
    myServer = server;
    myFile = new File(getDataDir(serverPaths), getId() + ".xml");
    myServer.addListener(this);
    loadState();
  }

  @Override
  public void serverShutdown() {
    saveState();
  }

  @NotNull
  protected abstract String getId();

  protected abstract void writeExternal(@NotNull Element element);

  protected abstract void readExternal(@NotNull Element element);

  @NotNull
  private File getDataDir(@NotNull final ServerPaths serverPaths) {
    try {
      return FileUtil.createDir(new File(serverPaths.getPluginDataDirectory(), "usage-statistics"));
    }
    catch (final IOException e) {
      ExceptionUtil.rethrowAsRuntimeException(e);
      //noinspection ConstantConditions
      return null;
    }
  }

  private void saveState() {
    final Element root = new Element("root");
    writeExternal(root);
    XmlUtil.saveXml(root, myFile);
  }

  private void loadState() {
    final Element root = XmlUtil.loadXml(myFile);
    if (root != null) {
      readExternal(root);
    }
  }
}
