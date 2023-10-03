/*
 * Copyright 2023 byteunderflow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.byteunderflow.remoteconsole;

import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.fusesource.jansi.Ansi.Attribute.*;
import static org.fusesource.jansi.Ansi.ansi;

public class RemoteConsole
{
    static final String DISCONNECT_COMMAND = ".exit";

    public static void main(String[] arguments) throws IOException, AuthenticationException
    {
        if (arguments.length != 3)
        {
            System.out.println(ansi().fgRgb(255, 0, 0).a("Please enter server address, port and password.").reset());
            return;
        }

        // Installing ANSI console
        AnsiConsole.systemInstall();

        // Parsing arguments
        final String address = arguments[0];
        final int port = Integer.parseInt(arguments[1]);
        final String password = arguments[2];

        // Connecting to server using address, port and password
        final Rcon rcon = new Rcon(address, port, password.getBytes(StandardCharsets.UTF_8));

        // Logging connection success and additional information
        System.out.println(ansi().fgRgb(255, 200, 0).a(UNDERLINE).a("(c) byteunderflow 2023").reset());
        System.out.println(ansi().fgRgb(0, 225, 31).a(String.format("Connection to server %s on port %d succeeded.", address, port)).reset());
        System.out.println(ansi().fgRgb(0, 225, 31).a("Disconnect command: ").a(ITALIC).a(DISCONNECT_COMMAND).reset());

        // Reading input from console
        final Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext())
        {
            final String command = scanner.nextLine();

            // Stop reading input when receiving disconnect command
            if (command.equalsIgnoreCase(DISCONNECT_COMMAND))
                break;

            // Executing command on server
            final String response = rcon.command(command);
            // Rendering response
            final String rendered = render(response);
            // Printing rendered response
            System.out.println(rendered);
            System.out.flush();
        }

        scanner.close();

        // Disconnecting from server
        rcon.disconnect();

        // Logging disconnection success
        System.out.println(ansi().fgRgb(0, 225, 31).a("Connection closed.").reset());
    }

    static final Map<Character, Integer> colors = new HashMap<>();
    static final Map<Character, Ansi.Attribute> attributes = new HashMap<>();

    static
    {
        // Mapping Minecraft color codes to RGB values
        colors.put('0', 0x000000);
        colors.put('1', 0x0000AA);
        colors.put('2', 0x00AA00);
        colors.put('3', 0x00AAAA);
        colors.put('4', 0xAA0000);
        colors.put('5', 0xAA00AA);
        colors.put('6', 0xFFAA00);
        colors.put('7', 0xAAAAAA);
        colors.put('8', 0x555555);
        colors.put('9', 0x5555FF);
        colors.put('a', 0x55FF55);
        colors.put('b', 0x55FFFF);
        colors.put('c', 0xFF5555);
        colors.put('d', 0xFF55FF);
        colors.put('e', 0xFFFF55);
        colors.put('f', 0xFFFFFF);

        // Mapping Minecraft attribute codes to ANSI attributes
        attributes.put('k', BLINK_FAST);
        attributes.put('l', INTENSITY_BOLD);
        attributes.put('m', STRIKETHROUGH_ON);
        attributes.put('n', UNDERLINE);
        attributes.put('o', ITALIC);
        attributes.put('r', RESET);
    }

    static char COLOR_SYMBOL = '§';

    static String render(String input)
    {
        // A buffer containing the rendered response
        final Ansi buffer = ansi();
        // Splitting input into individual symbols
        final char[] symbols = input.toCharArray();

        for (int index = 0; index < symbols.length; ++index)
        {
            final char symbol = symbols[index];

            if (symbol != COLOR_SYMBOL)
            {
                buffer.a(symbol);
                continue;
            }

            if (index >= symbols.length - 1)
                continue;

            final char code = symbols[++index];
            final Integer color = colors.get(code);
            final Ansi.Attribute attribute = attributes.get(code);

            // If the code refers to a color, set the corresponding RGB foreground color
            if (color != null)
                buffer.fgRgb(color);

            // If the code refers to an attribute, set the corresponding ANSI attribute
            if (attribute != null)
                buffer.a(attribute);
        }

        return buffer.reset().toString();
    }
}