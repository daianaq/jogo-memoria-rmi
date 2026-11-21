package servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Servidor {

    public static void main(String[] args) {

        try {
            
            Registry registro = LocateRegistry.createRegistry(1099);
  
            JogoMemoriaImpl jogo = new JogoMemoriaImpl();
           
            registro.rebind("JogoMemoria", jogo);

            System.out.println("Servidor iniciado com sucesso!");
            System.out.println("Aguardando os jogadores...");

        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor:");
            e.printStackTrace();
        }
    }
}