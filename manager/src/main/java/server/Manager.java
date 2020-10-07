package server;

import org.jline.builtins.Commands;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jcommander.JCommanderParameterResolverAutoConfiguration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.FileValueProvider;
import org.springframework.shell.standard.StandardAPIAutoConfiguration;
import org.springframework.shell.standard.commands.StandardCommandsAutoConfiguration;
import org.springframework.util.StringUtils;

/**
 * @author nano
 */
@Configuration
@Import({
        SpringShellAutoConfiguration.class,
        JLineShellAutoConfiguration.class,
        JCommanderParameterResolverAutoConfiguration.class,
        StandardAPIAutoConfiguration.class,
        StandardCommandsAutoConfiguration.class,
        BucketCommands.class,
        LucianCommands.class,
        Commands.class,
        FileValueProvider.class,
})
public class Manager {

    public static void main(String[] args) {
        String[] disabledCommands = {"--spring.shell.command.stacktrace.enabled=true", "-Dfile.encoding=UTF8", "-Dsun.jnu.encoding=UTF8"};
        String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);
        ConfigurableApplicationContext context = SpringApplication.run(Manager.class, fullArgs);
    }

    @Bean
    public PromptProvider promptProvider() {
        return new PromptProvider() {
            @Override
            public AttributedString getPrompt() {
                return new AttributedString("root$ >",
                        AttributedStyle.HIDDEN_OFF.foreground(AttributedStyle.BLACK));
            }
        };
    }

}